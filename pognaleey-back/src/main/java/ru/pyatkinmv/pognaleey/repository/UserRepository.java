package ru.pyatkinmv.pognaleey.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import ru.pyatkinmv.pognaleey.model.User;

public interface UserRepository extends CrudRepository<User, Long> {
  Optional<User> findByUsername(String username);

  boolean existsByUsername(String username);

  List<User> findAllByIdIn(Collection<Long> ids);
}
