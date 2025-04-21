package hu.bme.aut.crypto_casino_backend.repository;

import hu.bme.aut.crypto_casino_backend.model.Transaction;
import hu.bme.aut.crypto_casino_backend.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUser(User user);
}
