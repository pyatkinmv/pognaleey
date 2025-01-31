package ru.pyatkinmv.pognaleey.mapper;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import ru.pyatkinmv.pognaleey.dto.*;
import ru.pyatkinmv.pognaleey.model.*;
import ru.pyatkinmv.pognaleey.util.Utils;

@Component
public class TravelMapper {
  public static TravelInquiryDto toInquiryDto(TravelInquiry inquiry) {
    return TravelInquiryDto.builder()
        .id(inquiry.getId())
        .params(inquiry.getParams())
        .createdAt(inquiry.getCreatedAt())
        .build();
  }

  public static TravelRecommendationListDto toRecommendationListDto(
      Collection<TravelRecommendation> recommendations,
      Map<Long, ImageDto> idToImageMap,
      Map<Long, Long> recommendationIdToGuideIdMap) {
    return new TravelRecommendationListDto(
        recommendations.stream()
            .map(it -> toRecommendationDto(it, idToImageMap, recommendationIdToGuideIdMap))
            .sorted(Comparator.comparing(TravelRecommendationDto::id))
            .toList());
  }

  @SneakyThrows
  public static TravelRecommendationDto toRecommendationDto(
      TravelRecommendation recommendation,
      Map<Long, ImageDto> idToImageMap,
      Map<Long, Long> recommendationIdToGuideIdMap) {
    var details =
        Optional.ofNullable(recommendation.getDetails())
            .map(it -> Utils.toObject(it, GptResponseRecommendationDetailsDto.class))
            .map(it -> new TravelRecommendationDto.DetailsDto(it.description(), it.reasoning()))
            .orElse(null);
    var image =
        Optional.ofNullable(recommendation.getImageId())
            .map(idToImageMap::get)
            .filter(it -> !it.isWithoutUrls())
            .orElse(null);
    var guideId = recommendationIdToGuideIdMap.get(recommendation.getId());

    return new TravelRecommendationDto(
        recommendation.getId(),
        recommendation.getTitle(),
        mapToApi(recommendation.getStatus()),
        details,
        image,
        guideId);
  }

  public static List<TravelGuideInfoDto> toShortGuideListDto(
      List<TravelGuide> userGuides,
      List<UserDto> users,
      Map<Long, ImageDto> idToImageMap,
      Map<Long, Integer> guideIdToLikesCountMap,
      Set<Long> currentUserLikedGuidesIds) {
    var userIdToUser = users.stream().collect(Collectors.toMap(UserDto::id, it -> it));

    return userGuides.stream()
        .map(
            guide ->
                toGuideInfoDto(
                    guide,
                    Optional.ofNullable(guide.getUserId()).map(userIdToUser::get).orElse(null),
                    Optional.ofNullable(guide.getImageId()).map(idToImageMap::get).orElse(null),
                    guideIdToLikesCountMap.get(guide.getId()),
                    currentUserLikedGuidesIds.contains(guide.getId())))
        .sorted(Comparator.comparingInt(TravelGuideInfoDto::totalLikes).reversed())
        .toList();
  }

  public static TravelGuideInfoDto toGuideInfoDto(
      TravelGuide it,
      @Nullable UserDto user,
      @Nullable ImageDto image,
      int totalLikes,
      boolean currentUserLiked) {
    return new TravelGuideInfoDto(
        it.getId(),
        it.getTitle(),
        Optional.ofNullable(image).filter(ImageDto::isWithUrls).orElse(null),
        totalLikes,
        currentUserLiked,
        it.getCreatedAt().toEpochMilli(),
        user,
        it.getRecommendationId());
  }

  public static UserDto toUserDto(User user) {
    return new UserDto(user.getId(), user.getUsername());
  }

  public static TravelGuideContentDto toGuideContentDto(
      List<TravelGuideContentItem> items, Map<Long, ImageDto> guideIdToImageMap) {
    return new TravelGuideContentDto(
        items.stream()
            .map(it -> map(it, guideIdToImageMap))
            .sorted(Comparator.comparing(TravelGuideContentDto.TravelGuideContentItemDto::ordinal))
            .toList());
  }

  private static TravelGuideContentDto.TravelGuideContentItemDto map(
      TravelGuideContentItem item, Map<Long, ImageDto> guideIdToImageMap) {
    return new TravelGuideContentDto.TravelGuideContentItemDto(
        item.getId(),
        item.getGuideId(),
        item.getOrdinal(),
        extractContent(item, guideIdToImageMap.get(item.getId())),
        mapToApi(item.getStatus()),
        item.getType().name());
  }

  @Nullable
  private static String extractContent(TravelGuideContentItem item, @Nullable ImageDto image) {
    if (image != null && item.getType() == GuideContentItemType.IMAGE) {
      return image.isWithUrls() ? Utils.toJson(image) : null;
    } else {
      return item.getContent();
    }
  }

  public static Image toImage(ImageSearchClientImageDto dto, String title) {
    return new Image(
        null,
        Instant.now(),
        title,
        dto.url(),
        dto.thumbnailUrl(),
        dto.query(),
        false,
        dto.licenceUrl(),
        dto.authorName(),
        dto.authorUrl());
  }

  public static Image toImage(ImageDto dto) {
    return new Image(
        dto.id(),
        Instant.now(),
        dto.title(),
        dto.url(),
        dto.thumbnailUrl(),
        dto.query(),
        dto.aiGenerated(),
        dto.licenceUrl(),
        dto.authorName(),
        dto.authorUrl());
  }

  public static ImageDto toImageDto(Image image) {
    return new ImageDto(
        image.getId(),
        image.getTitle(),
        image.getUrl(),
        image.getThumbnailUrl(),
        image.getQuery(),
        image.getAiGenerated(),
        image.getLicenceUrl(),
        image.getAuthorName(),
        image.getAuthorUrl());
  }

  // TODO: утащить все FAILED с фронта
  public static String mapToApi(ProcessingStatus status) {
    return switch (status) {
      case IN_PROGRESS, CONTENT_GENERATED, IMAGE_SEARCH_FINISHED ->
          ProcessingStatus.IN_PROGRESS.name();
      default -> status.name();
    };
  }
}
