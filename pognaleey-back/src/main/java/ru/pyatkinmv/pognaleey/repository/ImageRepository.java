package ru.pyatkinmv.pognaleey.repository;


import org.springframework.data.repository.CrudRepository;
import ru.pyatkinmv.pognaleey.model.Image;

import java.util.Collection;
import java.util.List;
import java.util.stream.StreamSupport;

public interface ImageRepository extends CrudRepository<Image, Long> {

    List<Image> findAllByIdIn(Collection<Long> ids);

    List<Image> findAllByUrlIsNull();

    default List<Image> saveAllFromIterable(Iterable<Image> images) {
        return StreamSupport.stream(saveAll(images).spliterator(), false).toList();
    }
}
