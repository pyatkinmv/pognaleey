package ru.pyatkinmv.pognaleey.mapper;

import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import ru.pyatkinmv.pognaleey.dto.*;
import ru.pyatkinmv.pognaleey.dto.gpt.GptResponseRecommendationDetailsDto;
import ru.pyatkinmv.pognaleey.model.TravelGuide;
import ru.pyatkinmv.pognaleey.model.TravelInquiry;
import ru.pyatkinmv.pognaleey.model.TravelRecommendation;
import ru.pyatkinmv.pognaleey.model.User;
import ru.pyatkinmv.pognaleey.util.Utils;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class TravelMapper {

    public static TravelInquiryDto toInquiryDto(TravelInquiry inquiry, Collection<TravelRecommendation> recommendations) {
        return TravelInquiryDto.builder()
                .id(inquiry.getId())
                .payload(inquiry.getParams())
                .createdAt(inquiry.getCreatedAt())
                .quickRecommendations(recommendations.stream()
                        .map(it -> new TravelQuickRecommendationDto(
                                it.getId(),
                                it.getTitle(),
                                it.getShortDescription())
                        ).toList())
                .build();
    }

    public static TravelRecommendationListDto toRecommendationListDto(Collection<TravelRecommendation> recommendations) {
        return new TravelRecommendationListDto(
                recommendations.stream()
                        .map(it -> toRecommendationDto(it.getDetails(), it.getImageUrl()))
                        .toList()
        );
    }

    @SneakyThrows
    public static TravelRecommendationDto toRecommendationDto(String recommendationDetailsJson, String imageUrl) {
        var details = Utils.toObject(
                recommendationDetailsJson,
                GptResponseRecommendationDetailsDto.class
        );

        return new TravelRecommendationDto(
                details.title(),
                details.budget(),
                details.reasoning(),
                details.creativeDescription(),
                details.tips(),
                details.whereToGo(),
                details.additionalConsideration(),
                imageUrl
        );
    }

    public static TravelGuideFullDto toGuideDto(TravelGuide guide, User user, int totalLikes) {
        return new TravelGuideFullDto(
                guide.getId(),
                guide.getTitle(),
                guide.getImageUrl(),
                guide.getDetails(),
                totalLikes,
                Optional.ofNullable(user).map(TravelMapper::toUserDto).orElse(null)
        );
    }

    public static List<TravelGuideShortDto> toGuideListDto(List<TravelGuide> userGuides,
                                                           List<User> users,
                                                           Map<Long, Integer> guideIdToLikesCountMap) {
        var userIdToUser = users.stream().collect(Collectors.toMap(User::getId, it -> it));

        return userGuides.stream()
                .map(it -> new TravelGuideShortDto(
                                it.getId(),
                                it.getTitle(),
                                it.getImageUrl(),
                                guideIdToLikesCountMap.get(it.getId()),
                                Optional.ofNullable(userIdToUser.get(it.getUserId()))
                                        .map(TravelMapper::toUserDto)
                                        .orElse(null)
                        )
                )
                .sorted(Comparator.comparingInt(TravelGuideShortDto::totalLikes).reversed())
                .toList();
    }

    public static UserDto toUserDto(User user) {
        return new UserDto(user.getId(), user.getUsername());
    }
}
