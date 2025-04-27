package hu.bme.aut.crypto_casino_backend.repository;

import hu.bme.aut.crypto_casino_backend.model.BlockchainTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BlockchainTransactionRepository extends JpaRepository<BlockchainTransaction, Long> {
    List<BlockchainTransaction> findByUserAddressOrderByTimestampDesc(String userAddress);
}
