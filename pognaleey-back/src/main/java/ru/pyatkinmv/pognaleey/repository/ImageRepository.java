package ru.pyatkinmv.pognaleey.repository;


import org.springframework.data.repository.CrudRepository;
import ru.pyatkinmv.pognaleey.model.Image;

public interface ImageRepository extends CrudRepository<Image, Long> {
}
