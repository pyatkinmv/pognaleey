package ru.pyatkinmv.where_to_go.repository;

import org.springframework.data.repository.CrudRepository;
import ru.pyatkinmv.where_to_go.model.TravelRecommendation;

public interface TravelRecommendationRepository extends CrudRepository<TravelRecommendation, Long> {
    TravelRecommendation findByInquiryId(Long inquiryId);
}