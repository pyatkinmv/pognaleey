package ru.pyatkinmv.pognaleey.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import ru.pyatkinmv.pognaleey.dto.TravelGuideFullDto;
import ru.pyatkinmv.pognaleey.dto.TravelGuideLikeDto;
import ru.pyatkinmv.pognaleey.dto.TravelGuideShortListDto;
import ru.pyatkinmv.pognaleey.mapper.TravelMapper;
import ru.pyatkinmv.pognaleey.model.TravelGuide;
import ru.pyatkinmv.pognaleey.model.TravelGuideLike;
import ru.pyatkinmv.pognaleey.model.User;
import ru.pyatkinmv.pognaleey.repository.TravelGuideLikeRepository;
import ru.pyatkinmv.pognaleey.repository.TravelGuideRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class TravelGuideService {
    private final TravelGuideRepository guideRepository;
    private final TravelGuideLikeRepository likeRepository;

    public TravelGuideLikeDto likeGuide(long guideId, @Nullable User user) {
        // TODO: idempotency?
        if (user == null) {
            throw new IllegalArgumentException("user is null");
        }

        var guide = guideRepository.findById(guideId)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Guide %d not found", guideId)));

        likeRepository.save(
                TravelGuideLike.builder()
                        .userId(user.getId())
                        .createdAt(Instant.now())
                        .guideId(guide.getId()).build()
        );

        int totalLikes = likeRepository.countByGuideId(guideId);

        return new TravelGuideLikeDto(guide.getId(), true, totalLikes);
    }

    public TravelGuideLikeDto unlikeGuide(long guideId, @Nullable User user) {
        // TODO: idempotency?
        if (user == null) {
            throw new IllegalArgumentException("user is null");
        }

        var like = likeRepository.findByUserIdAndGuideId(user.getId(), guideId)
                .orElseThrow(() -> new IllegalArgumentException("Like not found"));

        likeRepository.delete(like);

        int totalLikes = likeRepository.countByGuideId(guideId);

        return new TravelGuideLikeDto(guideId, false, totalLikes);
    }

    public TravelGuideFullDto getFullGuide(long guideId, @Nullable User user) {
        var guide = guideRepository.findById(guideId)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Guide %d not found", guideId)));
        int totalLikes = likeRepository.countByGuideId(guideId);

        return TravelMapper.toGuideDto(guide, user, totalLikes);
    }

    public TravelGuideShortListDto getMyGuides(@Nullable User user) {
        if (user == null) {
            throw new IllegalArgumentException("user is null");
        }

        var userGuides = guideRepository.findByUserId(user.getId());
        var userGuidesIds = userGuides.stream().map(TravelGuide::getId).toList();
        var guideIdToLikesCountMap = guideRepository.findLikesCountByGuideIds(userGuidesIds);

        return TravelMapper.toGuideListDto(userGuides, user, guideIdToLikesCountMap);
    }

    public TravelGuideShortListDto getLikedGuides(@Nullable User user) {
        if (user == null) {
            throw new IllegalArgumentException("user is null");
        }

        var userGuidesLikes = likeRepository.findAllByUserId(user.getId());
        var userGuidesIds = userGuidesLikes.stream().map(TravelGuideLike::getGuideId).toList();
        var userGuides = guideRepository.findAllByIdIn(userGuidesIds);
        var guideIdToLikesCountMap = guideRepository.findLikesCountByGuideIds(userGuidesIds);

        return TravelMapper.toGuideListDto(userGuides, user, guideIdToLikesCountMap);
    }

    // TODO: pageable
    public TravelGuideShortListDto getFeedGuides(@Nullable User user) {
        var topGuideIdToLikeCountMap = guideRepository.findTopGuides(10);
        var topGuides = guideRepository.findAllByIdIn(topGuideIdToLikeCountMap.keySet());

        return TravelMapper.toGuideListDto(topGuides, user, topGuideIdToLikeCountMap);
    }

    public TravelGuideFullDto createGuide(long recommendationId, @Nullable User user) {
        // TODO: Implement
        var uuid = UUID.randomUUID().toString();
        var guide = TravelGuide.builder()
                .title("title-" + uuid)
                .details("details-" + uuid)
                .imageUrl("https://image-url.com")
                .recommendationId(recommendationId)
                .userId(Optional.ofNullable(user).map(it -> it.getId()).orElse(null))
                .createdAt(Instant.now())
                .build();
        guide = guideRepository.save(guide);
        int totalLikes = likeRepository.countByGuideId(guide.getId());

        return TravelMapper.toGuideDto(guide, user, totalLikes);
    }
}
