package ru.pyatkinmv.pognaleey.repository;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import ru.pyatkinmv.pognaleey.model.TravelRecommendation;

import java.util.Collection;
import java.util.function.Supplier;

public interface TravelRecommendationRepository extends CrudRepository<TravelRecommendation, Long> {
    Collection<TravelRecommendation> findByInquiryId(Long inquiryId);

    @Modifying
    @Query("""
                UPDATE travel_recommendations 
                SET DETAILS = :details
                WHERE id = :id
            """)
    int updateDetails(Long id, String details);

    static void withCheck(Supplier<Integer> updateSingle) {
        var updatedCount = updateSingle.get();

        if (updatedCount != 1) {
            throw new IllegalArgumentException("Couldn't update travel recommendations");
        }
    }

    @Modifying
    @Query("""
                UPDATE travel_recommendations 
                SET IMAGE_URL = :imageUrl
                WHERE id = :id
            """)
    int updateImageUrl(Long id, String imageUrl);

}