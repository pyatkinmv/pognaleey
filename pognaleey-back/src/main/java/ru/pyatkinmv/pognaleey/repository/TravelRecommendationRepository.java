package ru.pyatkinmv.pognaleey.repository;

import java.util.List;
import java.util.stream.StreamSupport;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import ru.pyatkinmv.pognaleey.model.TravelRecommendation;

public interface TravelRecommendationRepository extends CrudRepository<TravelRecommendation, Long> {
  List<TravelRecommendation> findByInquiryId(Long inquiryId);

  List<TravelRecommendation> findAllByIdIn(List<Long> ids);

  @Modifying
  @Query(
      """
          UPDATE travel_recommendations
          SET
              details = :details,
              status  = CASE
                            WHEN status = 'IMAGE_SEARCH_FINISHED' THEN 'READY'
                            WHEN status = 'IN_PROGRESS' THEN 'CONTENT_GENERATED'
                            ELSE status
                  END
          WHERE id=:id;
      """)
  void updateDetailsAndStatus(Long id, String details);

  @Modifying
  @Query(
      """
          UPDATE travel_recommendations
          SET
              image_id = :imageId,
              status = CASE
                          WHEN status = 'CONTENT_GENERATED' THEN 'READY'
                          WHEN status = 'IN_PROGRESS' THEN 'IMAGE_SEARCH_FINISHED'
                          ELSE status
                       END
          WHERE id = :id;
      """)
  void updateImageIdAndStatus(Long id, Long imageId);

  @Modifying
  @Query(
      """
          UPDATE travel_recommendations
          SET status = 'FAILED'
          WHERE id IN (:ids);
      """)
  void setFailed(List<Long> ids);

  @Modifying
  @Query(
      """
          UPDATE travel_recommendations
          SET status = :status
          WHERE id = :id;
      """)
  void setStatus(Long id, String status);

  default List<TravelRecommendation> saveAllFromIterable(
      Iterable<TravelRecommendation> travelRecommendations) {
    return StreamSupport.stream(saveAll(travelRecommendations).spliterator(), false).toList();
  }
}
