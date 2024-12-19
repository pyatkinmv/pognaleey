package ru.pyatkinmv.where_to_go.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.jdbc.repository.query.Query;
import ru.pyatkinmv.where_to_go.model.TravelInquiry;

import java.util.Optional;

public interface TravelInquiryRepository extends CrudRepository<TravelInquiry, Long> {
//    @Query("SELECT * FROM travel_inquiries ti LEFT JOIN travel_recommendations tr ON ti.id = tr.inquiry_id WHERE ti.id = :id")
//    Optional<TravelInquiry> findByIdWithRecommendation(Long id);
}
