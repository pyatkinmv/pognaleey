package ru.pyatkinmv.pognaleey.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.pyatkinmv.pognaleey.DatabaseCleaningTest;
import ru.pyatkinmv.pognaleey.dto.AuthRequestDto;
import ru.pyatkinmv.pognaleey.dto.TravelGuideLikeDto;
import ru.pyatkinmv.pognaleey.model.ProcessingStatus;
import ru.pyatkinmv.pognaleey.model.TravelGuide;
import ru.pyatkinmv.pognaleey.model.TravelInquiry;
import ru.pyatkinmv.pognaleey.model.TravelRecommendation;
import ru.pyatkinmv.pognaleey.repository.TravelGuideRepository;
import ru.pyatkinmv.pognaleey.repository.TravelInquiryRepository;
import ru.pyatkinmv.pognaleey.repository.TravelRecommendationRepository;
import ru.pyatkinmv.pognaleey.repository.UserRepository;
import ru.pyatkinmv.pognaleey.security.JwtAuthenticationToken;

import java.time.Instant;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class TravelGuideServiceTest extends DatabaseCleaningTest {
    @Autowired
    private TravelGuideService travelGuideService;
    @Autowired
    private TravelGuideRepository travelGuideRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TravelInquiryRepository travelInquiryRepository;
    @Autowired
    private TravelRecommendationRepository travelRecommendationRepository;

    @Test
    void createGuide() {
        var recommendation = createRecommendation();
        var guide = travelGuideService.createGuide(recommendation.getId());
        assertThat(guide.id()).isGreaterThan(0);
        assertThat(guide.title()).isNotNull();
        assertThat(guide.owner()).isNull();

        assertThrows(
                RuntimeException.class,
                () -> travelGuideService.createGuide(recommendation.getId())
        );

        var anotherRecommendation = createRecommendation();
        var userGuide = withUser(
                "user-1",
                () -> travelGuideService.createGuide(anotherRecommendation.getId())
        );
        assertThat(userGuide.id()).isNotEqualTo(guide.id());
        assertThat(userGuide.id()).isGreaterThan(0);
        assertThat(userGuide.title()).isNotNull();
        assertThat(userGuide.imageUrl()).isNotNull();
        assertThat(userGuide.owner()).isNotNull();
        assertThat(userGuide.totalLikes()).isEqualTo(0);
    }

    // TODO: fix
    @Test
    void getGuideInfo() {
        var guide = withUser("user-1", this::createTravelGuide);
//        var fullGuideDto = travelGuideService.getGuideInfo(guide.getId(), 1_000);
//        assertThat(fullGuideDto.id()).isGreaterThan(0);
//        assertThat(fullGuideDto.details()).isNotNull();
//        assertThat(fullGuideDto.owner()).isNotNull();
//        assertThat(fullGuideDto.owner().username()).isEqualTo("user-1");
    }

    @Test
    void likeGuide() {
        var guide = createTravelGuide();
        assertThrows(
                RuntimeException.class,
                () -> travelGuideService.likeGuide(guide.getId())
        );

        var likeDto = withUser("user-1", () -> travelGuideService.likeGuide(guide.getId()));
        assertThat(likeDto).isEqualTo(new TravelGuideLikeDto(guide.getId(), true, 1));

        likeDto = withUser("user-1", () -> travelGuideService.likeGuide(guide.getId()));
        assertThat(likeDto).isEqualTo(new TravelGuideLikeDto(guide.getId(), true, 1));

        likeDto = withUser("user-2", () -> travelGuideService.likeGuide(guide.getId()));
        assertThat(likeDto).isEqualTo(new TravelGuideLikeDto(guide.getId(), true, 2));
    }

    @Test
    void unlikeGuide() {
        var guide = createTravelGuide();
        assertThrows(
                RuntimeException.class,
                () -> travelGuideService.unlikeGuide(guide.getId())
        );

        var unlikeDto = withUser("user-1", () -> travelGuideService.unlikeGuide(guide.getId()));
        assertThat(unlikeDto).isEqualTo(new TravelGuideLikeDto(guide.getId(), false, 0));

        var likeDto = withUser("user-1", () -> travelGuideService.likeGuide(guide.getId()));
        assertThat(likeDto).isEqualTo(new TravelGuideLikeDto(guide.getId(), true, 1));

        likeDto = withUser("user-2", () -> travelGuideService.likeGuide(guide.getId()));
        assertThat(likeDto).isEqualTo(new TravelGuideLikeDto(guide.getId(), true, 2));

        unlikeDto = withUser("user-1", () -> travelGuideService.unlikeGuide(guide.getId()));
        assertThat(unlikeDto).isEqualTo(new TravelGuideLikeDto(guide.getId(), false, 1));

        unlikeDto = withUser("user-1", () -> travelGuideService.unlikeGuide(guide.getId()));
        assertThat(unlikeDto).isEqualTo(new TravelGuideLikeDto(guide.getId(), false, 1));

        unlikeDto = withUser("user-2", () -> travelGuideService.unlikeGuide(guide.getId()));
        assertThat(unlikeDto).isEqualTo(new TravelGuideLikeDto(guide.getId(), false, 0));
    }

    @Test
    void getMyGuides() {
        withUser("user-1", this::createTravelGuide);
        withUser("user-2", this::createTravelGuide);

        assertThrows(
                RuntimeException.class,
                () -> travelGuideService.getMyGuides(Pageable.ofSize(10))
        );

        var userGuidesPage = withUser("user-1", () -> travelGuideService.getMyGuides(Pageable.ofSize(10)));
        assertThat(userGuidesPage.getTotalElements()).isEqualTo(1);
        assertThat(userGuidesPage.getContent()).hasSize(1);
        var guideDto = userGuidesPage.getContent().getFirst();
        assertThat(guideDto.id()).isGreaterThan(0);
        assertThat(guideDto.title()).isNotNull();
        assertThat(guideDto.owner()).isNotNull();
        assertThat(guideDto.owner().username()).isEqualTo("user-1");
    }

    @Test
    void getFeedGuides() {
        withUser("user-1", this::createTravelGuide);
        withUser("user-2", this::createTravelGuide);

        var guides = travelGuideService.getFeedGuides(Pageable.ofSize(10));
        assertThat(guides).hasSize(2);

        guides = withUser("user-1", () -> travelGuideService.getFeedGuides(Pageable.ofSize(10)));
        assertThat(guides).hasSize(2);
    }

    @Test
    void getLikedGuides() {
        var user1Guide = withUser("user-1", this::createTravelGuide);
        var user2Guide = withUser("user-2", this::createTravelGuide);

        assertThrows(
                RuntimeException.class,
                () -> travelGuideService.getLikedGuides(Pageable.ofSize(10))
        );

        var guides = withUser("user-1", () -> travelGuideService.getLikedGuides(Pageable.ofSize(10)));
        assertThat(guides).isEmpty();

        withUser("user-1", () -> travelGuideService.likeGuide(user1Guide.getId()));
        guides = withUser("user-1", () -> travelGuideService.getLikedGuides(Pageable.ofSize(10)));
        assertThat(guides).hasSize(1);

        withUser("user-1", () -> travelGuideService.likeGuide(user2Guide.getId()));
        guides = withUser("user-1", () -> travelGuideService.getLikedGuides(Pageable.ofSize(10)));
        assertThat(guides).hasSize(2);

        guides = withUser("user-2", () -> travelGuideService.getLikedGuides(Pageable.ofSize(10)));
        assertThat(guides).isEmpty();
    }

    private <T> T withUser(String username, Supplier<T> supplier) {
        var user = userRepository.findByUsername(username)
                .orElseGet(() -> {
                    userService.registerUser(new AuthRequestDto(username, username + "-password"));
                    return userService.loadUserByUsername(username);
                });
        var authentication = new JwtAuthenticationToken(user);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        var result = supplier.get();
        SecurityContextHolder.getContext().setAuthentication(null);

        return result;
    }

    private TravelGuide createTravelGuide() {
        var recommendation = createRecommendation();
        var guide = travelGuideService.createGuide(recommendation.getId());

        return travelGuideRepository.findById(guide.id()).orElseThrow();
    }

    private TravelRecommendation createRecommendation() {
        var inquiryParams = "duration=1-3 days;to=Russia;budget=standard";
        var inquiry = travelInquiryRepository.save(new TravelInquiry(null, inquiryParams, Instant.now(), null));
        assertThat(inquiry.getParams()).isEqualTo(inquiryParams);
        assertThat(inquiry.getId()).isNotNull();
        var recommendation = travelRecommendationRepository.save(
                new TravelRecommendation(null, Instant.now(), inquiry.getId(), "Москва", "белокаменная", "details", "imageUrl", ProcessingStatus.IN_PROGRESS)
        );
        assertThat(recommendation.getId()).isNotNull();

        return recommendation;
    }

}