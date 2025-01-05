package ru.pyatkinmv.pognaleey.repository;

import org.springframework.data.repository.CrudRepository;
import ru.pyatkinmv.pognaleey.model.User;

import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Long> {
    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);
}
