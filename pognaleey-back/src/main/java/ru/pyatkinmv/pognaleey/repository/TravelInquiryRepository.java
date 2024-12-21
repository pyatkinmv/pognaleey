package ru.pyatkinmv.pognaleey.repository;

import org.springframework.data.repository.CrudRepository;
import ru.pyatkinmv.pognaleey.model.TravelInquiry;

public interface TravelInquiryRepository extends CrudRepository<TravelInquiry, Long> {
//    @Query("SELECT * FROM travel_inquiries ti LEFT JOIN travel_recommendations tr ON ti.id = tr.inquiry_id WHERE ti.id = :id")
//    Optional<TravelInquiry> findByIdWithRecommendation(Long id);
}
