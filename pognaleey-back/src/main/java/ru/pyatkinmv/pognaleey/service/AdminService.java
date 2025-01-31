package ru.pyatkinmv.pognaleey.service;

import java.io.*;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import ru.pyatkinmv.pognaleey.client.KandinskyImageGenerateHttpClient;
import ru.pyatkinmv.pognaleey.dto.AdminGuidesCreateDtoList;
import ru.pyatkinmv.pognaleey.dto.AdminUploadImageDto;
import ru.pyatkinmv.pognaleey.dto.ImageDto;
import ru.pyatkinmv.pognaleey.dto.TravelGuideInfoDto;
import ru.pyatkinmv.pognaleey.model.*;
import ru.pyatkinmv.pognaleey.repository.*;
import ru.pyatkinmv.pognaleey.util.Utils;
import ru.pyatkinmv.pognaleey.util.converter.Converters;
import ru.pyatkinmv.pognaleey.util.converter.ResourceConverter;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {
  private static final int MAX_FILE_SIZE_BYTES_TO_STORE_AS_IS = 512 * 1000;

  private final ResourceRepository resourceRepository;
  private final TravelGuideRepository guideRepository;
  private final TravelGuideContentItemRepository contentItemRepository;
  private final TravelRecommendationService recommendationService;
  private final TravelInquiryRepository inquiryRepository;
  private final ImageService imageService;
  private final TravelRecommendationRepository recommendationRepository;
  private final TravelGuideService guideService;

  private final TransactionTemplate transactionTemplate;

  private final TaskSchedulerService taskSchedulerService;
  private final KandinskyImageGenerateHttpClient imageGenerateHttpClient;
  private final ImageRepository imageRepository;

  @Value("${app.domain-name}")
  private String domainName;

  @Value("${server.servlet.context-path}")
  private String contextPath;

  public void uploadTitleImage(AdminUploadImageDto upload) {
    try (InputStream inputStream = upload.file().getInputStream()) {
      var guide = guideRepository.findById(upload.guideId()).orElseThrow();
      var recommendation = recommendationService.findById(guide.getRecommendationId());

      transactionTemplate.executeWithoutResult(
          it -> {
            var largeImageResourceId = saveResourceConvertingLarge(inputStream, upload);

            var largeImageResource = resourceRepository.findById(largeImageResourceId);
            var thumbnailImageResourceId =
                saveResourceConverting(
                    largeImageResource.data(),
                    Converters.JPG_512.get(),
                    upload.file().getOriginalFilename());

            var imageDto =
                new ImageDto(
                    null,
                    recommendation.getTitle(),
                    buildUrl(largeImageResourceId),
                    buildUrl(thumbnailImageResourceId),
                    recommendation.getTitle(),
                    upload.aiGenerated(),
                    null,
                    upload.authorName(),
                    upload.authorUrl());
            var savedImage = imageService.saveImage(imageDto);

            saveTitleContentItemForImage(guide.getId(), savedImage);

            recommendation.setImageId(savedImage.id());
            recommendationRepository.save(recommendation);
            log.info("Saved recommendation {}", recommendation);

            guide.setImageId(savedImage.id());
            guideRepository.save(guide);
            log.info("Saved guide {}", guide);
          });
    } catch (IOException e) {
      log.error("Failed to upload title image", e);
      throw new RuntimeException(e);
    }
  }

  @Async
  public void generateImageResourcesAsync(Long imageId) {
    var image = imageService.findByIdOrThrow(imageId);
    generateImageResourcesAndEnrichScheduling(image);
  }

  @Async
  public void generateImageResourcesForNotFoundAsync() {
    var images = imageRepository.findAllByUrlIsNull();
    images.forEach(image -> generateImageResourcesAsync(image.getId()));
  }

  @SneakyThrows
  public void generateImageResourcesAndEnrichScheduling(ImageDto image) {
    log.info("begin generateImageResourcesAndEnrichScheduling for image {}", image);

    if (image.isWithUrls()) {
      log.warn("Image already has urls");
    }

    var generatedImageAsync = imageGenerateHttpClient.generateImage(image.query()).orElseThrow();

    var uuid = generatedImageAsync.uuid();

    log.info("begin scheduleTask for: {}; time: {}", uuid, ZonedDateTime.now());
    var waitBeforeGet = (int) (generatedImageAsync.status_time() * 1.2);
    taskSchedulerService.scheduleTask(() -> getImageEnrichAndSave(uuid, image), waitBeforeGet);
  }

  private ImageDto getImageEnrichAndSave(String uuid, ImageDto image) {
    var result = imageGenerateHttpClient.getImage(uuid);
    log.info("end scheduleTask for: {}; time: {}", uuid, ZonedDateTime.now());

    if (result == null || result.images() == null || result.images().isEmpty()) {
      log.info("null response for {}", uuid);
      throw new RuntimeException("null response for " + uuid);
    }

    log.info(
        "imageGenerateHttpClient.getImage: uuid={}, status={}, content={}",
        result.uuid(),
        result.status(),
        result.images().getFirst().substring(0, 20));

    var resourceId =
        saveResourceConverting(result.images().getFirst(), Converters.BASE64.get(), uuid);

    var enriched =
        new ImageDto(
            image.id(),
            image.title(),
            buildUrl(resourceId),
            buildUrl(resourceId),
            image.query(),
            true,
            null,
            imageGenerateHttpClient.getClientName(),
            imageGenerateHttpClient.getClientWebUrl());

    var saved = imageService.saveImage(enriched);
    log.info("end getImageEnrichAndSave for image {}", saved);

    return saved;
  }

  private void saveTitleContentItemForImage(Long guideId, ImageDto savedImage) {
    var items =
        contentItemRepository.findByGuideId(guideId).stream()
            .sorted(Comparator.comparing(TravelGuideContentItem::getOrdinal))
            .toList();
    TravelGuideContentItem item;

    if (items.size() > 1 && items.get(1).getType() == GuideContentItemType.IMAGE) {
      item = items.get(1);
      item.setContent(Utils.toJson(savedImage));
    } else {
      item =
          new TravelGuideContentItem(
              null,
              guideId,
              Utils.toJson(savedImage),
              items.getFirst().getOrdinal() + 1,
              ProcessingStatus.READY,
              GuideContentItemType.IMAGE);
    }

    var saved = contentItemRepository.save(item);
    log.info("Saved content item {}", saved.getId());
  }

  private String buildUrl(Long resourceId) {
    return String.format("%s%s/resources/%d", domainName, contextPath, resourceId);
  }

  private Long saveResourceConvertingLarge(InputStream inputStream, AdminUploadImageDto upload) {
    if (upload.file().getSize() <= MAX_FILE_SIZE_BYTES_TO_STORE_AS_IS || upload.keepOriginal()) {
      return saveResourceConverting(
          inputStream, Converters.AS_IS.get(), upload.file().getOriginalFilename());
    } else {
      return saveResourceConverting(
          inputStream, Converters.JPG_1024.get(), upload.file().getOriginalFilename());
    }
  }

  public List<TravelGuideInfoDto> createGuide(AdminGuidesCreateDtoList guidesCreateDtoList) {
    return guidesCreateDtoList.guides().stream()
        .map(
            it -> {
              var inquiry =
                  inquiryRepository.save(
                      new TravelInquiry(null, it.inquiryParams(), Instant.now(), null));

              var recommendation =
                  recommendationRepository.save(
                      new TravelRecommendation(
                          null,
                          Instant.now(),
                          inquiry.getId(),
                          it.recommendationTitle(),
                          null,
                          "{\"reasoning\": \"reasoning\", \"description\": \"description\"}",
                          ProcessingStatus.READY));

              recommendationService.enrichWithImages(
                  List.of(recommendation),
                  List.of(
                      new TravelRecommendationService.TitleAndImageQuery(
                          recommendation.getTitle(), it.imageQuery())));
              return guideService.createGuide(recommendation.getId());
            })
        .toList();
  }

  /**
   * Saves a resource after converting it from its original format to another format.
   *
   * <p>This method takes an input of any type T, converts it using a provided converter, and then
   * saves the resulting InputStream using a resource repository. The entire process avoids loading
   * the full input data into memory, making it suitable for large files or streams.
   *
   * @param <T> the type of the input data
   * @param input the input data to convert and save
   * @param converter the converter responsible for transforming the input data into the desired
   *     format
   * @param originalFileName the original file name to use when building the resource name
   * @return the identifier of the saved resource
   * @throws RuntimeException if an I/O error occurs during conversion or saving
   */
  private <T> Long saveResourceConverting(
      T input, ResourceConverter<T> converter, String originalFileName) {
    try (InputStream stream = converter.convert(input)) {
      String resourceName = converter.buildResourceName(originalFileName);
      var saved = resourceRepository.save(resourceName, stream);
      log.info("Saved {} resource {}", resourceName, saved);

      return saved;
    } catch (IOException e) {
      throw new RuntimeException("Error saving the image", e);
    }
  }
}
