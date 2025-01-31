package ru.pyatkinmv.pognaleey.repository;

import java.util.List;
import java.util.stream.StreamSupport;
import org.springframework.data.repository.CrudRepository;
import ru.pyatkinmv.pognaleey.model.TravelGuideContentItem;

public interface TravelGuideContentItemRepository
    extends CrudRepository<TravelGuideContentItem, Long> {
  List<TravelGuideContentItem> findByGuideId(Long travelGuideId);

  default List<TravelGuideContentItem> saveAllFromIterable(Iterable<TravelGuideContentItem> items) {
    return StreamSupport.stream(saveAll(items).spliterator(), false).toList();
  }
}
