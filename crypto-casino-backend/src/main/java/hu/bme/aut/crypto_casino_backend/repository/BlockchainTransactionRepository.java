package hu.bme.aut.crypto_casino_backend.repository;

import hu.bme.aut.crypto_casino_backend.model.BlockchainTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlockchainTransactionRepository extends JpaRepository<BlockchainTransaction, Long> {
    Page<BlockchainTransaction> findByUserAddressOrderByTimestampDesc(String userAddress, Pageable pageable);

    List<BlockchainTransaction> findByTxHash(String txHash);

    Page<BlockchainTransaction> findByEventTypeOrderByTimestampDesc(
            BlockchainTransaction.TransactionType eventType, Pageable pageable);

    Page<BlockchainTransaction> findByGameAddressOrderByTimestampDesc(String gameAddress, Pageable pageable);
}
