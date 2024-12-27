package ru.pyatkinmv.pognaleey.repository;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import ru.pyatkinmv.pognaleey.model.TravelRecommendation;

import java.util.List;
import java.util.stream.StreamSupport;

public interface TravelRecommendationRepository extends CrudRepository<TravelRecommendation, Long> {
    List<TravelRecommendation> findByInquiryId(Long inquiryId);

    List<TravelRecommendation> findAllByIdIn(List<Long> ids);

    @Modifying
    @Query("""
                UPDATE travel_recommendations
                SET DETAILS = :details
                WHERE id = :id
            """)
    void updateDetails(Long id, String details);

    @Modifying
    @Query("""
                UPDATE travel_recommendations
                SET IMAGE_URL = :imageUrl
                WHERE id = :id
            """)
    void updateImageUrl(Long id, String imageUrl);

    default List<TravelRecommendation> saveAllFromIterable(Iterable<TravelRecommendation> travelRecommendations) {
        return StreamSupport.stream(saveAll(travelRecommendations).spliterator(), false).toList();
    }

}