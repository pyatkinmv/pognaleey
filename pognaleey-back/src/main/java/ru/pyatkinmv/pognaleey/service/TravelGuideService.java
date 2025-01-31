package ru.pyatkinmv.pognaleey.service;

import static ru.pyatkinmv.pognaleey.security.AuthenticatedUserProvider.getCurrentUser;
import static ru.pyatkinmv.pognaleey.security.AuthenticatedUserProvider.getCurrentUserOrThrow;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.pyatkinmv.pognaleey.dto.*;
import ru.pyatkinmv.pognaleey.mapper.TravelMapper;
import ru.pyatkinmv.pognaleey.model.GuideContentItemType;
import ru.pyatkinmv.pognaleey.model.TravelGuide;
import ru.pyatkinmv.pognaleey.model.TravelGuideContentItem;
import ru.pyatkinmv.pognaleey.model.TravelGuideLike;
import ru.pyatkinmv.pognaleey.repository.TravelGuideContentItemRepository;
import ru.pyatkinmv.pognaleey.repository.TravelGuideRepository;
import ru.pyatkinmv.pognaleey.util.Utils;

@Slf4j
@RequiredArgsConstructor
@Service
public class TravelGuideService {

  private final TravelGuideRepository guideRepository;
  private final TravelGuideLikeService likeService;
  private final TravelRecommendationService recommendationService;
  private final UserService userService;
  private final ExecutorService executorService;
  private final TravelGuideContentProvider guideContentProvider;
  private final TravelGuideContentItemRepository guideContentItemRepository;
  private final ImageService imageService;

  private static Long extractImageId(TravelGuideContentItem contentItem) {
    return Utils.toObject(contentItem.getContent(), ImageIdDto.class).imageId();
  }

  public TravelGuideLikeDto likeGuide(long guideId) {
    var guide =
        guideRepository
            .findById(guideId)
            .orElseThrow(
                () -> new IllegalArgumentException(String.format("Guide %d not found", guideId)));

    var user = getCurrentUserOrThrow();
    var doesntExist = likeService.findIdByUserIdAndGuideId(user.id(), guideId).isEmpty();

    if (doesntExist) {
      likeService.save(
          TravelGuideLike.builder()
              .userId(user.id())
              .createdAt(Instant.now())
              .guideId(guide.getId())
              .build());
    }

    int totalLikes = likeService.countByGuideId(guideId);

    return new TravelGuideLikeDto(guide.getId(), true, totalLikes);
  }

  public TravelGuideLikeDto unlikeGuide(long guideId) {
    likeService
        .findIdByUserIdAndGuideId(getCurrentUserOrThrow().id(), guideId)
        .ifPresent(likeService::deleteById);
    int totalLikes = likeService.countByGuideId(guideId);

    return new TravelGuideLikeDto(guideId, false, totalLikes);
  }

  public TravelGuideInfoDto getGuideInfo(long guideId) {
    var guide = findTravelGuide(guideId);
    int totalLikes = likeService.countByGuideId(guideId);
    var owner =
        Optional.ofNullable(guide.getUserId()).flatMap(userService::findUserById).orElse(null);
    var isCurrentUserLiked =
        getCurrentUser()
            .map(it -> likeService.findGuidesIdsByUserId(it.id(), Integer.MAX_VALUE, 0))
            .map(it -> it.contains(guideId))
            .orElse(false);
    var image =
        Optional.ofNullable(guide.getImageId()).map(imageService::findByIdOrThrow).orElse(null);

    return TravelMapper.toGuideInfoDto(guide, owner, image, totalLikes, isCurrentUserLiked);
  }

  private TravelGuide findTravelGuide(long guideId) {
    return guideRepository
        .findById(guideId)
        .orElseThrow(
            () -> new IllegalArgumentException(String.format("Guide %d not found", guideId)));
  }

  public Page<TravelGuideInfoDto> getMyGuides(Pageable pageable) {
    var user = getCurrentUserOrThrow();
    var totalCount = guideRepository.countAllByUserId(user.id());
    var offset = pageable.getPageSize() * pageable.getPageNumber();
    var guideIdToLikesCountMap =
        guideRepository.findTopGuides(
            user.id(),
            LanguageContextHolder.getLanguageOrDefault().name(),
            pageable.getPageSize(),
            offset);
    var userGuides = guideRepository.findAllByIdIn(guideIdToLikesCountMap.keySet());
    var currentUserLikedGuidesIds =
        getCurrentUser()
            .map(it -> likeService.findGuidesIdsByUserId(it.id(), Integer.MAX_VALUE, 0))
            .orElseGet(Set::of);
    var idToImageMap = getIdToImageMap(userGuides);
    var guides =
        TravelMapper.toShortGuideListDto(
            userGuides,
            List.of(user),
            idToImageMap,
            guideIdToLikesCountMap,
            currentUserLikedGuidesIds);

    return new PageImpl<>(guides, pageable, totalCount);
  }

  public Page<TravelGuideInfoDto> getLikedGuides(Pageable pageable) {
    var user = getCurrentUserOrThrow();
    var offset = pageable.getPageSize() * pageable.getPageNumber();
    var likedGuidesIds =
        likeService.findGuidesIdsByUserId(user.id(), pageable.getPageSize(), offset);

    if (likedGuidesIds.isEmpty()) {
      return Page.empty(pageable);
    }

    var userGuides = guideRepository.findAllByIdIn(likedGuidesIds);
    var guideIdToLikesCountMap = guideRepository.countLikesByGuideId(likedGuidesIds);
    var totalCount = likeService.countByUserId(user.id());
    var users = findUsersByGuides(userGuides);
    var idToImageMap = getIdToImageMap(userGuides);
    var guides =
        TravelMapper.toShortGuideListDto(
            userGuides, users, idToImageMap, guideIdToLikesCountMap, likedGuidesIds);

    return new PageImpl<>(guides, pageable, totalCount);
  }

  public Page<TravelGuideInfoDto> getFeedGuides(Pageable pageable) {
    var offset = pageable.getPageSize() * pageable.getPageNumber();
    var language = LanguageContextHolder.getLanguageOrDefault().name();
    var topGuideIdToLikeCountMap =
        guideRepository.findTopGuides(null, language, pageable.getPageSize(), offset);
    var topGuides = guideRepository.findAllByIdIn(topGuideIdToLikeCountMap.keySet());
    var totalCount = guideRepository.countAllByLanguage(language);
    var users = findUsersByGuides(topGuides);
    var currentUserLikedGuidesIds =
        getCurrentUser()
            .map(it -> likeService.findGuidesIdsByUserId(it.id(), Integer.MAX_VALUE, 0))
            .orElseGet(Set::of);
    var idToImageMap = getIdToImageMap(topGuides);
    var guides =
        TravelMapper.toShortGuideListDto(
            topGuides, users, idToImageMap, topGuideIdToLikeCountMap, currentUserLikedGuidesIds);

    return new PageImpl<>(guides, pageable, totalCount);
  }

  private Map<Long, ImageDto> getIdToImageMap(List<TravelGuide> guides) {
    var imageIds =
        guides.stream()
            .map(TravelGuide::getImageId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

    return imageService.getIdToImageMap(imageIds);
  }

  private List<UserDto> findUsersByGuides(List<TravelGuide> guides) {
    var usersIds =
        guides.stream()
            .map(TravelGuide::getUserId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

    return userService.findUsersByIds(usersIds);
  }

  public TravelGuideInfoDto createGuide(long recommendationId) {
    log.info("begin createGuide for recommendation: {}", recommendationId);
    var user = getCurrentUser().orElse(null);
    var recommendation = recommendationService.findById(recommendationId);
    var guide =
        guideRepository.save(
            TravelGuide.builder()
                .title(null)
                .imageId(recommendation.getImageId())
                .recommendationId(recommendationId)
                .userId(Optional.ofNullable(user).map(UserDto::id).orElse(null))
                .createdAt(Instant.now())
                .language(LanguageContextHolder.getLanguageOrDefault())
                .build());
    // TODO: image везде есть, но на фронт нулевой не передаем

    var guideContentItems =
        guideContentProvider.createBlueprintContentItems(
            guide.getId(), recommendation.getTitle(), recommendation.getImageId());

    executorService.execute(
        () ->
            guideContentProvider.enrichGuideWithContent(
                guide,
                guideContentItems,
                recommendation.getInquiryId(),
                recommendation.getTitle()));
    var image =
        Optional.ofNullable(guide.getImageId()).map(imageService::findByIdOrThrow).orElse(null);

    return TravelMapper.toGuideInfoDto(guide, user, image, 0, false);
  }

  @SneakyThrows
  public TravelGuideContentDto getGuideContent(long guideId) {
    List<TravelGuideContentItem> contentItems = guideContentItemRepository.findByGuideId(guideId);
    var guideIdToImageIdMap =
        contentItems.stream()
            .filter(it -> it.getType() == GuideContentItemType.IMAGE)
            .collect(
                Collectors.toMap(
                    TravelGuideContentItem::getId, TravelGuideService::extractImageId));
    var imageIdToImageMap =
        imageService.findAll(guideIdToImageIdMap.values()).stream()
            .collect(Collectors.toMap(ImageDto::id, it -> it));
    var guideIdToImageMap =
        guideIdToImageIdMap.entrySet().stream()
            .collect(
                Collectors.toMap(Map.Entry::getKey, it -> imageIdToImageMap.get(it.getValue())));

    return TravelMapper.toGuideContentDto(contentItems, guideIdToImageMap);
  }
}
