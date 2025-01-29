package ru.pyatkinmv.pognaleey.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;
import ru.pyatkinmv.pognaleey.client.KandinskyImageGenerateHttpClient;
import ru.pyatkinmv.pognaleey.dto.ImageDto;
import ru.pyatkinmv.pognaleey.dto.ManualGuidesCreateDtoList;
import ru.pyatkinmv.pognaleey.dto.TravelGuideInfoDto;
import ru.pyatkinmv.pognaleey.model.*;
import ru.pyatkinmv.pognaleey.repository.*;
import ru.pyatkinmv.pognaleey.util.Utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {
    private static final int MAX_FILE_SIZE_BYTES_TO_STORE_AS_IS = 512 * 1000;
    public static final String JPG = "jpg";


    private final ResourceRepository resourceRepository;
    private final TravelGuideRepository guideRepository;
    private final TravelGuideContentItemRepository contentItemRepository;
    private final TravelRecommendationService recommendationService;
    private final TravelInquiryRepository inquiryRepository;
    private final ImageService imageService;
    private final TravelRecommendationRepository recommendationRepository;
    private final TravelGuideContentProviderV2 contentProvider;
    private final TravelGuideService guideService;

    private final TransactionTemplate transactionTemplate;
    private final ExecutorService executorService;

    private final TaskSchedulerService taskSchedulerService;
    private final KandinskyImageGenerateHttpClient imageGenerateHttpClient;
    private final ImageRepository imageRepository;

    @Value("${app.domain-name}")
    private String domainName;

    @Value("${server.servlet.context-path}")
    private String contextPath;


    // TODO: add test
    public void uploadTitleImage(UploadImageDto upload) {
        try (InputStream inputStream = upload.file.getInputStream()) {
            var guide = guideRepository.findById(upload.guideId).orElseThrow();
            var recommendation = recommendationService.findById(guide.getRecommendationId());

            transactionTemplate.executeWithoutResult(it -> {
                var largeImageResourceId = saveResourceConvertingLarge(inputStream, upload);

                var largeImageResource = resourceRepository.findById(largeImageResourceId);
                var thumbnailImageResourceId = saveResourceConverting(largeImageResource.data(),
                        Converters.JPG_512.get(), upload.file().getOriginalFilename());

                var imageDto = new ImageDto(null, recommendation.getTitle(), buildUrl(largeImageResourceId),
                        buildUrl(thumbnailImageResourceId), recommendation.getTitle(), upload.aiGenerated, null,
                        upload.authorName, upload.authorUrl);
                var savedImage = imageService.saveImage(imageDto);

                saveTitleContentItemForImage(guide.getId(), savedImage);

                recommendation.setImageId(savedImage.id());
                recommendationRepository.save(recommendation);
                log.info("Saved recommendation {}", recommendation);

                guide.setImageId(savedImage.id());
                guideRepository.save(guide);
                log.info("Saved guide {}", guide);
            });
        } catch (Exception e) {
            log.error("Failed to upload title image", e);
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

        var generatedImageAsync = imageGenerateHttpClient.generateImage(image.query())
                .orElseThrow();

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

        log.info("imageGenerateHttpClient.getImage: uuid={}, status={}, content={}",
                result.uuid(), result.status(), result.images().getFirst().substring(0, 20));

        var resourceId = saveResourceConverting(result.images().getFirst(), Converters.BASE64.get(), uuid);

        var enriched = new ImageDto(image.id(),
                image.title(),
                buildUrl(resourceId),
                buildUrl(resourceId),
                image.query(),
                true,
                null,
                imageGenerateHttpClient.getClientName(),
                imageGenerateHttpClient.getClientWebUrl()
        );

        var saved = imageService.saveImage(enriched);
        log.info("end getImageEnrichAndSave for image {}", saved);

        return saved;
    }

    private void saveTitleContentItemForImage(Long guideId, ImageDto savedImage) {
        var items = contentItemRepository.findByGuideId(guideId)
                .stream().sorted(Comparator.comparing(TravelGuideContentItem::getOrdinal)).toList();
        TravelGuideContentItem item;

        if (items.size() > 1 && items.get(1).getType() == GuideContentItemType.IMAGE) {
            item = items.get(1);
            item.setContent(Utils.toJson(savedImage));
        } else {
            item = new TravelGuideContentItem(null, guideId, Utils.toJson(savedImage),
                    items.getFirst().getOrdinal() + 1, ProcessingStatus.READY, GuideContentItemType.IMAGE);
        }

        var saved = contentItemRepository.save(item);
        log.info("Saved content item {}", saved.getId());
    }

    private String buildUrl(Long resourceId) {
        return String.format("%s%s/resources/%d", domainName, contextPath, resourceId);
    }

    private Long saveResourceConvertingLarge(InputStream inputStream, UploadImageDto upload) {
        if (upload.file().getSize() <= MAX_FILE_SIZE_BYTES_TO_STORE_AS_IS || upload.keepOriginal()) {
            return saveResourceConverting(inputStream, Converters.AS_IS.get(), upload.file.getOriginalFilename());
        } else {
            return saveResourceConverting(inputStream, Converters.JPG_1024.get(), upload.file().getOriginalFilename());
        }
    }

    public List<TravelGuideInfoDto> createGuide(ManualGuidesCreateDtoList guidesCreateDtoList) {
        return
                guidesCreateDtoList.guides()
                        .stream()
                        .map(it -> {
                            var inquiry = inquiryRepository.save(new TravelInquiry(
                                    null,
                                    it.inquiryParams(),
                                    Instant.now(),
                                    null
                            ));

                            var recommendation = recommendationRepository.save(
                                    new TravelRecommendation(null,
                                            Instant.now(),
                                            inquiry.getId(),
                                            it.recommendationTitle(),
                                            null,
                                            "{\"reasoning\": \"reasoning\", \"description\": \"description\"}",
                                            ProcessingStatus.READY
                                    )
                            );

                            recommendationService.enrichWithImages(
                                    List.of(recommendation),
                                    List.of(new TravelRecommendationService.TitleAndImageQuery(recommendation.getTitle(), it.imageQuery()))
                            );
                            return guideService.createGuide(recommendation.getId());
//                    recommendationService.enrichRecommendationsAsync(List.of(recommendation), inquiry.getParams());
                        }).toList();
    }

    // NOTE: All this magic is necessary to avoid load file in host memory
    private <T> Long saveResourceConverting(T input, ResourceConverter<T> converter, String originalFileName) {
        try (InputStream stream = converter.convert(input)) {
            String resourceName = converter.buildResourceName(originalFileName);
            var saved = resourceRepository.save(resourceName, stream);
            log.info("Saved {} resource {}", resourceName, saved);

            return saved;
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при сохранении изображения", e);
        }
    }

    @RequiredArgsConstructor
    enum Converters {
        JPG_1024(new ThumbnailsConverter(1024, JPG, 1.0)),
        JPG_512(new ThumbnailsConverter(512, JPG, 1.0)),
        AS_IS(new AsIsConverter()),
        BASE64(new Base64Converter());

        private final ResourceConverter<?> converter;

        private <T> ResourceConverter<T> get() {
            //noinspection unchecked
            return (ResourceConverter<T>) converter;
        }
    }

    private interface ResourceConverter<T> {
        InputStream convert(T t) throws IOException;

        String buildResourceName(String originalFileName);
    }

    private static class AsIsConverter implements ResourceConverter<InputStream> {

        @Override
        public InputStream convert(InputStream inputStream) {
            return inputStream;
        }

        @Override
        public String buildResourceName(@Nullable String originalFileName) {
            return Optional.ofNullable(originalFileName).orElseGet(() -> UUID.randomUUID().toString());
        }
    }

    private static abstract class PipedResourceConverter<T> implements ResourceConverter<T> {
        @Override
        public InputStream convert(T input) throws IOException {
            var pipedOutputStream = new PipedOutputStream();
            var pipedInputStream = new PipedInputStream(pipedOutputStream);

            // TODO: Replace with executorService call
            new Thread(() -> {
                try (pipedOutputStream) {
                    doConvert(input, pipedOutputStream);
                } catch (IOException e) {
                    throw new RuntimeException("Ошибка обработки изображения", e);
                }
            }).start();

            return pipedInputStream;
        }

        abstract void doConvert(T input, PipedOutputStream outputStream) throws IOException;
    }

    @RequiredArgsConstructor
    private static class ThumbnailsConverter extends PipedResourceConverter<InputStream> {
        private static final String FILENAME_FORMAT = "%s-%d.%s";

        private final Integer width;
        private final String extension;
        private final Double quality;

        @SneakyThrows
        void doConvert(InputStream input, PipedOutputStream outputStream) {
            Thumbnails.of(input)
                    .width(Objects.requireNonNull(width))
                    .outputFormat(extension)
                    .outputQuality(Objects.requireNonNull(quality))
                    .toOutputStream(outputStream);

        }

        @Override
        public String buildResourceName(String base) {
            return String.format(FILENAME_FORMAT, base, width, extension);
        }
    }

    private static class Base64Converter extends PipedResourceConverter<String> {
        private final String extension = JPG;

        @Override
        void doConvert(String imageBase64, PipedOutputStream outputStream) throws IOException {
            byte[] imageBytes = Base64.getDecoder().decode(imageBase64);

            try (ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes)) {
                BufferedImage image = ImageIO.read(bis);
                // Сохраняем изображение в указанный формат
                ImageIO.write(image, extension, outputStream);
            }
        }

        @Override
        public String buildResourceName(String baseName) {
            return String.format("%s.%s", baseName, extension);
        }
    }

    public record UploadImageDto(MultipartFile file, Long guideId, boolean aiGenerated, boolean keepOriginal,
                                 @Nullable String authorName, @Nullable String authorUrl) {
    }

}
