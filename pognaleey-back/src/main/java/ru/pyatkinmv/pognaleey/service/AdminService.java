package ru.pyatkinmv.pognaleey.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;
import ru.pyatkinmv.pognaleey.dto.ImageDto;
import ru.pyatkinmv.pognaleey.model.GuideContentItemType;
import ru.pyatkinmv.pognaleey.model.ProcessingStatus;
import ru.pyatkinmv.pognaleey.model.TravelGuideContentItem;
import ru.pyatkinmv.pognaleey.repository.ResourceRepository;
import ru.pyatkinmv.pognaleey.repository.TravelGuideContentItemRepository;
import ru.pyatkinmv.pognaleey.repository.TravelGuideRepository;
import ru.pyatkinmv.pognaleey.repository.TravelRecommendationRepository;
import ru.pyatkinmv.pognaleey.util.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {
    private static final int MAX_FILE_SIZE_BYTES_TO_STORE_AS_IS = 512 * 1000;

    private final ResourceRepository resourceRepository;
    private final TravelGuideRepository guideRepository;
    private final TravelGuideContentItemRepository contentItemRepository;
    private final TravelRecommendationService recommendationService;
    private final ImageService imageService;
    private final TravelRecommendationRepository recommendationRepository;

    private final TransactionTemplate transactionTemplate;
    private final ExecutorService executorService;

    @Value("${app.domain-name}")
    private String domainName;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    private static void converting(InputStream imageInputStream, PipedOutputStream pipedOutputStream, ConvertType type) {
        try (pipedOutputStream) {
            Thumbnails.of(imageInputStream)
                    .width(type.width)
                    .outputFormat(type.extension)
                    .outputQuality(type.quality)
                    .toOutputStream(pipedOutputStream);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка обработки изображения", e);
        }
    }

    // TODO: add test
    public void uploadTitleImage(UploadImageDto upload) {
        try (InputStream inputStream = upload.file.getInputStream()) {
            var guide = guideRepository.findById(upload.guideId).orElseThrow();
            var recommendation = recommendationService.findById(guide.getRecommendationId());

            transactionTemplate.executeWithoutResult(it -> {
                var largeImageResourceId = saveResourceConvertingLarge(inputStream, upload);

                var largeImageResource = resourceRepository.findById(largeImageResourceId);
                var thumbnailImageResourceId = saveResourceConverting(largeImageResource.data(),
                        upload.file().getOriginalFilename(), ConvertType.THUMBNAIL);

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
            return resourceRepository.save(
                    ConvertType.buildBaseResourceName(upload.file().getOriginalFilename()),
                    inputStream
            );
        } else {
            return saveResourceConverting(inputStream, upload.file().getOriginalFilename(), ConvertType.LARGE);
        }
    }

    // NOTE: All this magic is necessary to avoid load file in host memory
    private Long saveResourceConverting(InputStream imageInputStream, String originalFileName, ConvertType convertType) {
        try (PipedOutputStream pipedOutputStream = new PipedOutputStream();
             PipedInputStream pipedInputStream = new PipedInputStream(pipedOutputStream)) {

            // Run converting from imageInputStream to pipedOutputStream in a separate thread
            executorService.execute(() -> converting(imageInputStream, pipedOutputStream, convertType));

            var saved = resourceRepository.save(convertType.buildResourceName(originalFileName), pipedInputStream);
            log.info("Saved {} resource {}", convertType.name(), saved);

            return saved;
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при сохранении изображения", e);
        }
    }

    @RequiredArgsConstructor
    private enum ConvertType {
        LARGE(1024, "jpg", 1.0),
        THUMBNAIL(512, "jpg", 1.0);

        private static final String FILENAME_FORMAT = "%s-%d.%s";
        private final int width;
        private final String extension;
        private final double quality;

        static String buildBaseResourceName(String originalFileName) {
            return Optional.ofNullable(originalFileName).orElseGet(() -> UUID.randomUUID().toString());
        }

        String buildResourceName(@Nullable String original) {
            var base = buildBaseResourceName(original);

            return String.format(FILENAME_FORMAT, base, width, extension);
        }
    }

    public record UploadImageDto(MultipartFile file, Long guideId, boolean aiGenerated, boolean keepOriginal,
                                 @Nullable String authorName, @Nullable String authorUrl) {
    }

}
