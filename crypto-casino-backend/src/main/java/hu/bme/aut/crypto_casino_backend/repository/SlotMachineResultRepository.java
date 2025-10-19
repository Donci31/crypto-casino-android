package hu.bme.aut.crypto_casino_backend.repository;

import hu.bme.aut.crypto_casino_backend.model.SlotMachineResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SlotMachineResultRepository extends JpaRepository<SlotMachineResult, Long> {

	Optional<SlotMachineResult> findByGameSessionId(Long gameSessionId);

}
