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
                SET
                    details = :details,
                    status = CASE
                                WHEN image_id IS NOT NULL THEN 'READY'
                                ELSE status
                             END
                WHERE id = :id;
            """)
    void updateDetailsAndStatus(Long id, String details);

    @Modifying
    @Query("""
                UPDATE travel_recommendations
                SET
                    image_id = :imageId,
                    status = CASE
                                WHEN details IS NOT NULL THEN 'READY'
                                ELSE status
                             END
                WHERE id = :id;
            """)
    void updateImageIdAndStatus(Long id, Long imageId);

    @Modifying
    @Query("""
                UPDATE travel_recommendations
                SET status = 'FAILED'
                WHERE id IN (:ids);
            """)
    void setFailed(List<Long> ids);

    @Modifying
    @Query("""
                UPDATE travel_recommendations
                SET status = :status
                WHERE id = :id;
            """)
    void setStatus(Long id, String status);

    default List<TravelRecommendation> saveAllFromIterable(Iterable<TravelRecommendation> travelRecommendations) {
        return StreamSupport.stream(saveAll(travelRecommendations).spliterator(), false).toList();
    }

}