package hu.bme.aut.crypto_casino_backend.repository;

import hu.bme.aut.crypto_casino_backend.model.Transaction;
import hu.bme.aut.crypto_casino_backend.model.User;
import hu.bme.aut.crypto_casino_backend.model.Wallet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUserOrderByTransactionTimeDesc(User user);
    Page<Transaction> findByUser(User user, Pageable pageable);
    List<Transaction> findByWalletOrderByTransactionTimeDesc(Wallet wallet);
    Page<Transaction> findByWallet(Wallet wallet, Pageable pageable);
    List<Transaction> findByTypeAndStatusOrderByTransactionTimeDesc(
            Transaction.TransactionType type, Transaction.TransactionStatus status);

    long countByUserAndType(User user, Transaction.TransactionType type);
}
