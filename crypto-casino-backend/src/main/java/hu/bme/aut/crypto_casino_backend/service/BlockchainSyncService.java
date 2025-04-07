package hu.bme.aut.crypto_casino_backend.service;

import hu.bme.aut.crypto_casino_backend.model.Wallet;
import hu.bme.aut.crypto_casino_backend.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class BlockchainSyncService {

    private final WalletRepository walletRepository;
    private final WalletService walletService;


    @Scheduled(fixedRate = 5, timeUnit = TimeUnit.MINUTES)
    public void syncAllWalletsWithBlockchain() {
        log.info("Starting scheduled blockchain synchronization");

        List<Wallet> wallets = walletRepository.findAll();

        for (Wallet wallet : wallets) {
            try {
                if (wallet.getEthereumAddress() != null) {
                    walletService.syncWalletWithBlockchain(wallet);
                    log.info("Synced wallet: {}", wallet.getEthereumAddress());
                }
            } catch (Exception e) {
                log.error("Error syncing wallet {}: {}", wallet.getEthereumAddress(), e.getMessage());
            }
        }

        log.info("Completed blockchain synchronization for {} wallets", wallets.size());
    }
}
