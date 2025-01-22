package ru.pyatkinmv.pognaleey.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.pyatkinmv.pognaleey.dto.TravelGuideContentDto;
import ru.pyatkinmv.pognaleey.dto.TravelGuideInfoDto;
import ru.pyatkinmv.pognaleey.dto.TravelGuideLikeDto;
import ru.pyatkinmv.pognaleey.mapper.TravelMapper;
import ru.pyatkinmv.pognaleey.model.TravelGuide;
import ru.pyatkinmv.pognaleey.model.TravelGuideLike;
import ru.pyatkinmv.pognaleey.model.User;
import ru.pyatkinmv.pognaleey.repository.TravelGuideContentItemRepository;
import ru.pyatkinmv.pognaleey.repository.TravelGuideRepository;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import static ru.pyatkinmv.pognaleey.security.AuthenticatedUserProvider.getCurrentUser;
import static ru.pyatkinmv.pognaleey.security.AuthenticatedUserProvider.getCurrentUserOrThrow;

@Slf4j
@RequiredArgsConstructor
@Service
public class TravelGuideService {

    private final TravelGuideRepository guideRepository;
    private final TravelGuideLikeService likeService;
    private final TravelRecommendationService recommendationService;
    private final UserService userService;
    private final ExecutorService executorService;
    private final TravelGuideContentProviderV2 guideContentProvider;
    private final TravelGuideContentItemRepository guideContentItemRepository;

    public TravelGuideLikeDto likeGuide(long guideId) {
        var guide = guideRepository.findById(guideId)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Guide %d not found", guideId)));

        var user = getCurrentUserOrThrow();
        var doesntExist = likeService.findByUserIdAndGuideId(user.getId(), guideId).isEmpty();

        if (doesntExist) {
            likeService.save(
                    TravelGuideLike.builder()
                            .userId(user.getId())
                            .createdAt(Instant.now())
                            .guideId(guide.getId()).build()
            );
        }

        int totalLikes = likeService.countByGuideId(guideId);

        return new TravelGuideLikeDto(guide.getId(), true, totalLikes);
    }

    public TravelGuideLikeDto unlikeGuide(long guideId) {
        likeService.findByUserIdAndGuideId(getCurrentUserOrThrow().getId(), guideId)
                .ifPresent(likeService::delete);
        int totalLikes = likeService.countByGuideId(guideId);

        return new TravelGuideLikeDto(guideId, false, totalLikes);
    }

    public TravelGuideInfoDto getGuideInfo(long guideId) {
        var guide = findTravelGuide(guideId);
        int totalLikes = likeService.countByGuideId(guideId);
        var owner = Optional.ofNullable(guide.getUserId())
                .flatMap(userService::findUserById)
                .orElse(null);
        var isCurrentUserLiked = getCurrentUser()
                .map(it -> likeService.findGuidesIdsByUserId(it.getId(), Integer.MAX_VALUE, 0))
                .map(it -> it.contains(guideId))
                .orElse(false);

        return TravelMapper.toGuideInfoDto(guide, owner, totalLikes, isCurrentUserLiked);
    }

    private TravelGuide findTravelGuide(long guideId) {
        return guideRepository.findById(guideId)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Guide %d not found", guideId)));
    }

    public Page<TravelGuideInfoDto> getMyGuides(Pageable pageable) {
        var user = getCurrentUserOrThrow();
        var totalCount = guideRepository.countAllByUserId(user.getId());
        var offset = pageable.getPageSize() * pageable.getPageNumber();
        var guideIdToLikesCountMap = guideRepository.findTopGuides(user.getId(), pageable.getPageSize(), offset);
        var userGuides = guideRepository.findAllByIdIn(guideIdToLikesCountMap.keySet());
        var currentUserLikedGuidesIds = getCurrentUser()
                .map(it -> likeService.findGuidesIdsByUserId(it.getId(), Integer.MAX_VALUE, 0))
                .orElseGet(Set::of);
        var guides = TravelMapper.toShortGuideListDto(userGuides, List.of(user), guideIdToLikesCountMap,
                currentUserLikedGuidesIds);

        return new PageImpl<>(guides, pageable, totalCount);
    }

    public Page<TravelGuideInfoDto> getLikedGuides(Pageable pageable) {
        var user = getCurrentUserOrThrow();
        var offset = pageable.getPageSize() * pageable.getPageNumber();
        var likedGuidesIds = likeService.findGuidesIdsByUserId(user.getId(), pageable.getPageSize(), offset);

        if (likedGuidesIds.isEmpty()) {
            return Page.empty(pageable);
        }

        var userGuides = guideRepository.findAllByIdIn(likedGuidesIds);
        var guideIdToLikesCountMap = guideRepository.countLikesByGuideId(likedGuidesIds);
        var totalCount = likeService.countByUserId(user.getId());
        var users = findUsersByGuides(userGuides);
        var guides = TravelMapper.toShortGuideListDto(userGuides, users, guideIdToLikesCountMap, likedGuidesIds);

        return new PageImpl<>(guides, pageable, totalCount);
    }

    public Page<TravelGuideInfoDto> getFeedGuides(Pageable pageable) {
        var offset = pageable.getPageSize() * pageable.getPageNumber();
        var topGuideIdToLikeCountMap = guideRepository.findTopGuides(null, pageable.getPageSize(), offset);
        var topGuides = guideRepository.findAllByIdIn(topGuideIdToLikeCountMap.keySet());
        var totalCount = guideRepository.count();
        var users = findUsersByGuides(topGuides);
        var currentUserLikedGuidesIds = getCurrentUser()
                .map(it -> likeService.findGuidesIdsByUserId(it.getId(), Integer.MAX_VALUE, 0))
                .orElseGet(Set::of);
        var guides = TravelMapper.toShortGuideListDto(topGuides, users, topGuideIdToLikeCountMap,
                currentUserLikedGuidesIds);

        return new PageImpl<>(guides, pageable, totalCount);
    }

    private List<User> findUsersByGuides(List<TravelGuide> guides) {
        var usersIds = guides.stream()
                .map(TravelGuide::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        return userService.findUsersByIds(usersIds);
    }

    public TravelGuideInfoDto createGuide(long recommendationId) {
        log.info("begin createGuide for recommendation: {}", recommendationId);
        var user = getCurrentUser().orElse(null);
        var recommendation = recommendationService.findById(recommendationId);
        var guide = guideRepository.save(
                TravelGuide.builder()
                        .title(null)
                        .imageUrl(recommendation.getImageUrl())
                        .recommendationId(recommendationId)
                        .userId(Optional.ofNullable(user).map(User::getId).orElse(null))
                        .createdAt(Instant.now())
                        .build()
        );

        var guideContentItems = guideContentProvider.createBlueprintContentItems(
                guide.getId(),
                recommendation.getTitle(),
                recommendation.getImageUrl()
        );

        executorService.execute(
                () -> guideContentProvider.enrichGuideWithContent(
                        guide,
                        guideContentItems,
                        recommendation.getInquiryId(),
                        recommendation.getTitle()
                )
        );

        return TravelMapper.toGuideInfoDto(guide, user, 0, false);
    }

    @SneakyThrows
    public TravelGuideContentDto getGuideContent(long guideId) {
//        Thread.sleep(3000);
        return TravelMapper.toGuideContentDto(guideContentItemRepository.findByGuideId(guideId));
    }
}
