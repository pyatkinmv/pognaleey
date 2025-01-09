package ru.pyatkinmv.pognaleey.repository;

import org.springframework.data.repository.CrudRepository;
import ru.pyatkinmv.pognaleey.model.TravelGuideLike;

import java.util.List;
import java.util.Optional;


public interface TravelGuideLikeRepository extends CrudRepository<TravelGuideLike, Long> {
    int countByGuideId(long guideId);

    Optional<TravelGuideLike> findByUserIdAndGuideId(long userId, long guideId);

    List<TravelGuideLike> findAllByUserId(Long userId);
}
