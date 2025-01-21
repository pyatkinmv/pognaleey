package ru.pyatkinmv.pognaleey.repository;

import org.springframework.data.repository.CrudRepository;
import ru.pyatkinmv.pognaleey.model.TravelGuideContentItem;

import java.util.List;

public interface TravelGuideContentItemRepository extends CrudRepository<TravelGuideContentItem, Long> {
    List<TravelGuideContentItem> findByGuideId(Long travelGuideId);
}
