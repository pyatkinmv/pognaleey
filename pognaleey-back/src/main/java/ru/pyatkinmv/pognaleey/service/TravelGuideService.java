package ru.pyatkinmv.pognaleey.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.pyatkinmv.pognaleey.dto.TravelGuideFullDto;
import ru.pyatkinmv.pognaleey.dto.TravelGuideLikeDto;
import ru.pyatkinmv.pognaleey.dto.TravelGuideShortDto;
import ru.pyatkinmv.pognaleey.mapper.TravelMapper;
import ru.pyatkinmv.pognaleey.model.TravelGuide;
import ru.pyatkinmv.pognaleey.model.TravelGuideLike;
import ru.pyatkinmv.pognaleey.model.User;
import ru.pyatkinmv.pognaleey.repository.TravelGuideRepository;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static ru.pyatkinmv.pognaleey.security.AuthenticatedUserProvider.getCurrentUser;
import static ru.pyatkinmv.pognaleey.security.AuthenticatedUserProvider.getCurrentUserOrThrow;

@Slf4j
@RequiredArgsConstructor
@Service
public class TravelGuideService {
    private final TravelGuideRepository guideRepository;
    private final TravelGuideLikeService likeService;
    private final UserService userService;

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

    public TravelGuideFullDto getFullGuide(long guideId) {
        var guide = guideRepository.findById(guideId)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Guide %d not found", guideId)));
        int totalLikes = likeService.countByGuideId(guideId);
        var owner = Optional.ofNullable(guide.getUserId())
                .flatMap(userService::findUserById)
                .orElse(null);

        return TravelMapper.toGuideDto(guide, owner, totalLikes);
    }

    public Page<TravelGuideShortDto> getMyGuides(Pageable pageable) {
        var user = getCurrentUserOrThrow();
        var totalCount = guideRepository.countAllByUserId(user.getId());
        var offset = pageable.getPageSize() * pageable.getPageNumber();
        var guideIdToLikesCountMap = guideRepository.findTopGuides(user.getId(), pageable.getPageSize(), offset);
        var userGuides = guideRepository.findAllByIdIn(guideIdToLikesCountMap.keySet());
        var guides = TravelMapper.toGuideListDto(userGuides, List.of(user), guideIdToLikesCountMap);

        return new PageImpl<>(guides, pageable, totalCount);
    }

    public Page<TravelGuideShortDto> getLikedGuides(Pageable pageable) {
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
        var guides = TravelMapper.toGuideListDto(userGuides, users, guideIdToLikesCountMap);

        return new PageImpl<>(guides, pageable, totalCount);
    }

    public Page<TravelGuideShortDto> getFeedGuides(Pageable pageable) {
        var offset = pageable.getPageSize() * pageable.getPageNumber();
        var topGuideIdToLikeCountMap = guideRepository.findTopGuides(null, pageable.getPageSize(), offset);
        var topGuides = guideRepository.findAllByIdIn(topGuideIdToLikeCountMap.keySet());
        var totalCount = guideRepository.count();
        var users = findUsersByGuides(topGuides);
        var guides = TravelMapper.toGuideListDto(topGuides, users, topGuideIdToLikeCountMap);

        return new PageImpl<>(guides, pageable, totalCount);
    }

    private List<User> findUsersByGuides(List<TravelGuide> guides) {
        var usersIds = guides.stream()
                .map(TravelGuide::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        return userService.findUsersByIds(usersIds);
    }

    public TravelGuideFullDto createGuide(long recommendationId) {
        var user = getCurrentUser().orElse(null);
        // TODO: Implement method
        var uuid = UUID.randomUUID().toString();
        var guide = guideRepository.save(
                TravelGuide.builder()
                        .title("title-" + uuid)
                        .details("details-" + uuid)
                        .imageUrl("https://image-url.com")
                        .recommendationId(recommendationId)
                        .userId(Optional.ofNullable(user).map(User::getId).orElse(null))
                        .createdAt(Instant.now())
                        .build()
        );

        return TravelMapper.toGuideDto(guide, user, 0);
    }
}
