package hu.bme.aut.crypto_casino_backend.repository;

import hu.bme.aut.crypto_casino_backend.model.GameSession;
import hu.bme.aut.crypto_casino_backend.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameSessionRepository extends JpaRepository<GameSession, Long> {

  Page<GameSession> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

  List<GameSession> findTop10ByUserOrderByCreatedAtDesc(User user);

  Page<GameSession> findByGameTypeOrderByCreatedAtDesc(String gameType, Pageable pageable);

  List<GameSession> findByUserId(Long userId);

}
