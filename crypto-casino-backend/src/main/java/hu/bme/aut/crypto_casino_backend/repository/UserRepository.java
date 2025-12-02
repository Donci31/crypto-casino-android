package hu.bme.aut.crypto_casino_backend.repository;

import hu.bme.aut.crypto_casino_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByUsername(String username);

  boolean existsByUsername(String username);

  boolean existsByEmail(String email);

  Optional<User> findByUsernameOrEmail(String username, String email);

}
