package hu.bme.aut.crypto_casino_backend.repository;

import hu.bme.aut.crypto_casino_backend.model.DiceResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.Optional;

@Repository
public interface DiceResultRepository extends JpaRepository<DiceResult, Long> {

	Optional<DiceResult> findByGameSessionId(Long gameSessionId);

	Optional<DiceResult> findByGameId(BigInteger gameId);

}
