package hu.bme.aut.crypto_casino_backend.repository;

import hu.bme.aut.crypto_casino_backend.model.UserWallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserWalletRepository extends JpaRepository<UserWallet, Long> {

  Optional<UserWallet> findByAddress(String address);

  List<UserWallet> findByUserId(Long userId);

  Optional<UserWallet> findByUserIdAndIsPrimaryTrue(Long userId);

  boolean existsByAddress(String address);

  @Modifying
  @Query("UPDATE UserWallet w SET w.isPrimary = false WHERE w.user.id = :userId AND w.id != :walletId")
  void unsetPrimaryForAllExcept(@Param("userId") Long userId, @Param("walletId") Long walletId);

  @Modifying
  @Query("UPDATE UserWallet w SET w.isPrimary = true WHERE w.id = :walletId")
  void setPrimary(@Param("walletId") Long walletId);

}
