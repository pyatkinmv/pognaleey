package ru.pyatkinmv.pognaleey.mapper;

import lombok.SneakyThrows;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import ru.pyatkinmv.pognaleey.dto.*;
import ru.pyatkinmv.pognaleey.model.TravelGuide;
import ru.pyatkinmv.pognaleey.model.TravelInquiry;
import ru.pyatkinmv.pognaleey.model.TravelRecommendation;
import ru.pyatkinmv.pognaleey.model.User;
import ru.pyatkinmv.pognaleey.util.Utils;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class TravelMapper {
    public static TravelInquiryDto toInquiryDto(TravelInquiry inquiry) {
        return TravelInquiryDto.builder()
                .id(inquiry.getId())
                .params(inquiry.getParams())
                .createdAt(inquiry.getCreatedAt())
                .build();
    }

    public static TravelRecommendationListDto toRecommendationListDto(Collection<TravelRecommendation> recommendations,
                                                                      Map<Long, Long> recommendationIdToGuideIdMap) {
        return new TravelRecommendationListDto(
                recommendations.stream()
                        .map(it -> toRecommendationDto(it, recommendationIdToGuideIdMap.get(it.getId())))
                        .sorted(Comparator.comparing(TravelRecommendationDto::id))
                        .toList()
        );
    }

    @SneakyThrows
    public static TravelRecommendationDto toRecommendationDto(TravelRecommendation recommendation,
                                                              @Nullable Long guideId) {
        var details = Optional.ofNullable(recommendation.getDetails())
                .map(it -> Utils.toObject(it, GptResponseRecommendationDetailsDto.class))
                .map(it -> new TravelRecommendationDto.DetailsDto(it.description(), it.reasoning()))
                .orElse(null);
        var image = Optional.ofNullable(recommendation.getImageUrl())
                .map(it -> new TravelRecommendationDto.ImageDto(it, it))
                .orElse(null);

        return new TravelRecommendationDto(
                recommendation.getId(),
                recommendation.getTitle(),
                recommendation.getStatus().name(),
                details,
                image,
                guideId
        );
    }

    public static TravelGuideFullDto toGuideDto(TravelGuide guide, @Nullable User user, int totalLikes) {
        return new TravelGuideFullDto(
                guide.getId(),
                guide.getTitle(),
                guide.getImageUrl(),
                guide.getDetails(),
                totalLikes,
                Optional.ofNullable(user).map(TravelMapper::toUserDto).orElse(null)
        );
    }

    public static List<TravelGuideShortDto> toShortGuideListDto(List<TravelGuide> userGuides,
                                                                List<User> users,
                                                                Map<Long, Integer> guideIdToLikesCountMap,
                                                                Set<Long> currentUserLikedGuidesIds) {
        var userIdToUser = users.stream().collect(Collectors.toMap(User::getId, it -> it));

        return userGuides.stream()
                .map(guide -> toShortGuideDto(
                                guide,
                                userIdToUser.get(guide.getUserId()),
                        guideIdToLikesCountMap.get(guide.getId()),
                        currentUserLikedGuidesIds.contains(guide.getId())
                        )
                )
                .sorted(Comparator.comparingInt(TravelGuideShortDto::totalLikes).reversed())
                .toList();
    }

    public static TravelGuideShortDto toShortGuideDto(TravelGuide it, @Nullable User user, int totalLikes,
                                                      boolean currentUserLiked) {
        return new TravelGuideShortDto(
                it.getId(),
                it.getTitle(),
                it.getImageUrl(),
                totalLikes,
                currentUserLiked,
                Optional.ofNullable(user)
                        .map(TravelMapper::toUserDto)
                        .orElse(null)
        );
    }

    public static UserDto toUserDto(User user) {
        return new UserDto(user.getId(), user.getUsername());
    }
}
