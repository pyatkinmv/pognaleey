package ru.pyatkinmv.pognaleey.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import ru.pyatkinmv.pognaleey.client.GptHttpClient;
import ru.pyatkinmv.pognaleey.client.ImagesSearchHttpClient;
import ru.pyatkinmv.pognaleey.dto.TravelGuideFullDto;
import ru.pyatkinmv.pognaleey.dto.TravelGuideLikeDto;
import ru.pyatkinmv.pognaleey.dto.TravelGuideShortDto;
import ru.pyatkinmv.pognaleey.mapper.TravelMapper;
import ru.pyatkinmv.pognaleey.model.TravelGuide;
import ru.pyatkinmv.pognaleey.model.TravelGuideLike;
import ru.pyatkinmv.pognaleey.model.User;
import ru.pyatkinmv.pognaleey.repository.TravelGuideRepository;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import static ru.pyatkinmv.pognaleey.security.AuthenticatedUserProvider.getCurrentUser;
import static ru.pyatkinmv.pognaleey.security.AuthenticatedUserProvider.getCurrentUserOrThrow;
import static ru.pyatkinmv.pognaleey.service.GptAnswerResolveHelper.parseSearchableItems;

@Slf4j
@RequiredArgsConstructor
@Service
public class TravelGuideService {
    private static final String MARKDOWN_IMAGE_FORMAT = "<img src=\"%s\" alt=\"%s\" width=\"700\">";

    private final TravelGuideRepository guideRepository;
    private final TravelGuideLikeService likeService;
    private final TravelRecommendationService recommendationService;
    private final TravelInquiryService inquiryService;
    private final UserService userService;
    private final GptHttpClient gptHttpClient;
    private final ImagesSearchHttpClient imagesSearchHttpClient;
    private final ExecutorService executorService;

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

    private static String generateGuidePrompt(String guideTitle,
                                              String inquiryParams,
                                              List<GptAnswerResolveHelper.SearchableItem> searchableGuideItems) {
        var titleToImagePhraseMap = searchableGuideItems.stream()
                .collect(Collectors.toMap(GptAnswerResolveHelper.SearchableItem::title, GptAnswerResolveHelper.SearchableItem::imageSearchPhrase));
        var guideTopics = String.join("|", titleToImagePhraseMap.keySet());

        return PromptService.generateCreateGuidePrompt(guideTitle, inquiryParams, guideTopics);
    }

    /**
     * Extracts the title from the guide content by removing the curly braces `{}`
     * around the first substring found. If no such substring is found, logs a warning
     * and returns `null`.
     *
     * @param guideContent The guide content to extract the title from.
     * @return The title without curly braces, or `null` if no title is found.
     *
     * <p>Example usage:</p>
     * {@code resolveGuideTitle("# {Guide Title} text");} returns {@code "Guide Title"}.
     * {@code resolveGuideTitle("No braces here");} returns null.
     */
    @Nullable
    private static String resolveGuideTitle(String guideContent) {
        var regex = "\\{.*?\\}";
        var titleWithBracketsOpt = GptAnswerResolveHelper.findFirstByRegex(guideContent, regex);

        if (titleWithBracketsOpt.isEmpty()) {
            log.warn("Could not find title for guide: {}...", guideContent.substring(0, 100));

            return null;
        }

        var titleWithBrackets = titleWithBracketsOpt.get();
        var titleWithoutBraces = titleWithBrackets.replaceAll("\\{", "").replaceAll("}", "");

        return GptAnswerResolveHelper.replaceQuotes(titleWithoutBraces);
    }

    private static String enrichGuideWithTitleImage(String guideContent, @Nullable String title,
                                                    String titleImageUrl) {
        if (title == null) {
            return guideContent;
        }

        var titleWithBrackets = String.format("{%s}", title);
        var target = String.format("%s\n" + MARKDOWN_IMAGE_FORMAT, title, titleImageUrl, title);

        if (guideContent.startsWith(titleWithBrackets)) {
            target = "# " + target;
        }

        var result = guideContent.replace(titleWithBrackets, target);
        log.info("Resolved content for title image: {}...", result.substring(0, 100));

        return result;
    }

    private static String enrichGuideWithContentImages(String guideDetailsWithoutImages, Map<String, String> titleToImageUrlMap) {
        var result = guideDetailsWithoutImages;

        for (var title : titleToImageUrlMap.keySet()) {
            var target = String.format("{%s}", title);
            var titleStr = GptAnswerResolveHelper.replaceQuotes(title);
            var replacement = String.format(MARKDOWN_IMAGE_FORMAT + "\n", titleToImageUrlMap.get(title), titleStr);
            result = result.replace(target, replacement);
        }

        return result;
    }

    @SneakyThrows
    public TravelGuideFullDto createGuide(long recommendationId) {
        log.info("begin createGuide for recommendation: {}", recommendationId);
        var user = getCurrentUser().orElse(null);
        var recommendation = recommendationService.findById(recommendationId);
        var inquiry = inquiryService.findById(recommendation.getInquiryId());

        var recommendationTitle = recommendation.getTitle();
        var guideImagesPrompt = PromptService.generateGuideImagesPrompt(recommendationTitle, inquiry.getParams());
        var imagesGuideResponseRaw = gptHttpClient.ask(guideImagesPrompt);
        var searchableGuideItems = parseSearchableItems(imagesGuideResponseRaw);

        var result = executorService.submit(() -> searchImagesWithSleepAndBuildTitleToImageMap(searchableGuideItems));

        var createGuidePrompt = generateGuidePrompt(recommendationTitle, inquiry.getParams(), searchableGuideItems);
        var guideContentRaw = gptHttpClient.ask(createGuidePrompt);
        var guideContentTitle = resolveGuideTitle(guideContentRaw);

        var titleToImageUrlMap = result.get();

        var guideContent = Optional.of(guideContentRaw)
                .map(it -> enrichGuideWithContentImages(guideContentRaw, titleToImageUrlMap))
                .map(it -> enrichGuideWithTitleImage(it, guideContentTitle, recommendation.getImageUrl()))
                .map(GptAnswerResolveHelper::stripCurlyBraces)
                .orElseThrow();

        var guide = guideRepository.save(
                TravelGuide.builder()
                        .title(Optional.ofNullable(guideContentTitle).orElse(recommendationTitle))
                        .details(guideContent)
                        .imageUrl(recommendation.getImageUrl())
                        .recommendationId(recommendationId)
                        .userId(Optional.ofNullable(user).map(User::getId).orElse(null))
                        .createdAt(Instant.now())
                        .build()
        );
        var guideDto = TravelMapper.toGuideDto(guide, user, 0);
        log.info("end createGuide for recommendation: {}, guide: {}", recommendationId, guide.getId());

        return guideDto;
    }

    private Map<String, String> searchImagesWithSleepAndBuildTitleToImageMap(List<GptAnswerResolveHelper.SearchableItem> titlesWithImageSearchPhrases) {
        return titlesWithImageSearchPhrases.stream()
                .collect(Collectors.toMap(
                        GptAnswerResolveHelper.SearchableItem::title,
                        it -> imagesSearchHttpClient.searchImageUrlWithRateLimiting(it.imageSearchPhrase()),
                        (a, b) -> b,
                        () -> new TreeMap<>(String.CASE_INSENSITIVE_ORDER)
                ));
    }
}
