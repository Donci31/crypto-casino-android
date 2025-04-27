package hu.bme.aut.crypto_casino_backend.repository;

import hu.bme.aut.crypto_casino_backend.model.BlockchainTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlockchainTransactionRepository extends JpaRepository<BlockchainTransaction, Long> {

    List<BlockchainTransaction> findByUserAddressOrderByTimestampDesc(String userAddress);

    Optional<BlockchainTransaction> findByTxHashAndUserAddress(String txHash, String userAddress);
}
