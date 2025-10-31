package hu.bme.aut.crypto_casino_backend.repository;

import hu.bme.aut.crypto_casino_backend.model.RouletteResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RouletteResultRepository extends JpaRepository<RouletteResult, Long> {

	Optional<RouletteResult> findByGameSessionId(Long gameSessionId);

}
