package hu.bme.aut.crypto_casino_backend.service;

import hu.bme.aut.crypto_casino_backend.config.Web3jConfig;
import hu.bme.aut.crypto_casino_backend.model.Transaction;
import hu.bme.aut.crypto_casino_backend.model.User;
import hu.bme.aut.crypto_casino_backend.model.Wallet;
import hu.bme.aut.crypto_casino_backend.repository.TransactionRepository;
import hu.bme.aut.crypto_casino_backend.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.casinotoken.CasinoToken;
import org.web3j.casinowallet.CasinoWallet;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.Key;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletService {

    private final Web3j web3j;
    private final CasinoToken casinoToken;
    private final CasinoWallet casinoWallet;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final Web3jConfig web3jConfig;
    private final ContractGasProvider gasProvider;

    @Value("${wallet.password-encryption-key}")
    private String encryptionKey;

    private final Map<String, String> testWallets = new HashMap<>() {{
        put("0x70997970C51812dc3A010C7d01b50e0d17dc79C8", "0x59c6995e998f97a5a0044966f0945389dc9e86dae88c7a8412f4603b6b78690d");
        put("0x3C44CdDdB6a900fa2b585dd299e03d12FA4293BC", "0x5de4111afa1a4b94908f83103eb1f1706367c2e68ca870fc3fb9a804cdab365a");
    }};

    private int currentWalletIndex = 0;

    
    @Transactional
    public Wallet createWalletForUser(User user) throws Exception {
        
        String[] addresses = testWallets.keySet().toArray(new String[0]);
        String walletAddress = addresses[currentWalletIndex % addresses.length];
        String privateKey = testWallets.get(walletAddress);

        
        currentWalletIndex++;

        
        String encryptedPrivateKey = encryptPrivateKey(privateKey);

        
        Wallet wallet = Wallet.builder()
                .user(user)
                .ethereumAddress(walletAddress)
                .encryptedKey(encryptedPrivateKey) 
                .casinoTokenBalance(BigDecimal.ZERO)
                .ethereumBalance(BigDecimal.ZERO)
                .blockchainRegistered(false)
                .build();

        walletRepository.save(wallet);

        
        user.setWallet(wallet);
        user.setWalletInitialized(true);

        
        syncWalletWithBlockchain(wallet);

        return wallet;
    }

    
    @Transactional
    public void registerWalletOnBlockchain(Wallet wallet) throws Exception {
        if (wallet.isBlockchainRegistered()) {
            log.info("Wallet {} already registered on blockchain", wallet.getEthereumAddress());
            return;
        }

        
        Credentials credentials = getMasterWalletCredentials();

        try {
            
            TransactionReceipt receipt = casinoWallet.registerUser(wallet.getEthereumAddress()).send();

            if (receipt.isStatusOK()) {
                wallet.setBlockchainRegistered(true);
                wallet.setLastSynced(System.currentTimeMillis());
                walletRepository.save(wallet);

                log.info("Wallet {} registered on blockchain, tx hash: {}",
                        wallet.getEthereumAddress(), receipt.getTransactionHash());
            } else {
                log.error("Failed to register wallet on blockchain: {}", receipt.getStatus());
                throw new RuntimeException("Blockchain transaction failed: " + receipt.getStatus());
            }
        } catch (Exception e) {
            log.error("Error registering wallet on blockchain", e);
            throw new RuntimeException("Failed to register wallet on blockchain", e);
        }
    }

    
    @Transactional
    public Transaction purchaseTokens(Wallet wallet, BigDecimal ethAmount) throws Exception {
        Credentials credentials = getMasterWalletCredentials();

        
        BigInteger weiAmount = Convert.toWei(ethAmount, Convert.Unit.ETHER).toBigInteger();

        
        Transaction transaction = Transaction.builder()
                .user(wallet.getUser())
                .wallet(wallet)
                .amount(ethAmount)
                .type(Transaction.TransactionType.EXCHANGE_ETH_TO_TOKEN)
                .status(Transaction.TransactionStatus.PENDING)
                .ethereumAmount(ethAmount)
                .transactionTime(LocalDateTime.now())
                .build();

        transactionRepository.save(transaction);

        try {
            
            TransactionReceipt receipt = casinoToken.purchaseTokensFor(wallet.getEthereumAddress(), weiAmount)
                    .send();

            if (receipt.isStatusOK()) {
                
                BigDecimal tokenAmount = ethAmount.multiply(BigDecimal.valueOf(100));

                
                wallet.setEthereumBalance(wallet.getEthereumBalance().subtract(ethAmount));
                wallet.setCasinoTokenBalance(wallet.getCasinoTokenBalance().add(tokenAmount));
                walletRepository.save(wallet);

                
                transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
                transaction.setTransactionHash(receipt.getTransactionHash());
                transaction.setBlockNumber(receipt.getBlockNumber().longValue());
                transaction.setCasinoTokenAmount(tokenAmount);
                transactionRepository.save(transaction);

                log.info("Purchased {} tokens for {} ETH, tx hash: {}",
                        tokenAmount, ethAmount, receipt.getTransactionHash());

                return transaction;
            } else {
                transaction.setStatus(Transaction.TransactionStatus.FAILED);
                transactionRepository.save(transaction);

                log.error("Token purchase failed: {}", receipt.getStatus());
                throw new RuntimeException("Token purchase failed: " + receipt.getStatus());
            }
        } catch (Exception e) {
            transaction.setStatus(Transaction.TransactionStatus.FAILED);
            transactionRepository.save(transaction);

            log.error("Error purchasing tokens", e);
            throw new RuntimeException("Failed to purchase tokens", e);
        }
    }

    
    @Transactional
    public Transaction withdrawTokens(Wallet wallet, BigDecimal tokenAmount) throws Exception {
        
        Credentials userCredentials = getWalletCredentials(wallet);

        
        Transaction transaction = Transaction.builder()
                .user(wallet.getUser())
                .wallet(wallet)
                .amount(tokenAmount)
                .type(Transaction.TransactionType.EXCHANGE_TOKEN_TO_ETH)
                .status(Transaction.TransactionStatus.PENDING)
                .casinoTokenAmount(tokenAmount)
                .transactionTime(LocalDateTime.now())
                .build();

        transactionRepository.save(transaction);

        try {
            CasinoToken userTokenContract = CasinoToken.load(
                    casinoToken.getContractAddress(),
                    web3j,
                    userCredentials,
                    gasProvider
            );

            TransactionReceipt approvalReceipt = userTokenContract.approve(
                    casinoToken.getContractAddress(),
                    tokenAmount.toBigInteger()
            ).send();

            if (!approvalReceipt.isStatusOK()) {
                transaction.setStatus(Transaction.TransactionStatus.FAILED);
                transactionRepository.save(transaction);
                throw new RuntimeException("Token approval failed");
            }

            Credentials masterCredentials = getMasterWalletCredentials();

            TransactionReceipt receipt = casinoToken.withdrawTokensFor(
                    wallet.getEthereumAddress(),
                    tokenAmount.toBigInteger()
            ).send();

            if (receipt.isStatusOK()) {
                BigDecimal ethAmount = tokenAmount.divide(BigDecimal.valueOf(100));

                wallet.setCasinoTokenBalance(wallet.getCasinoTokenBalance().subtract(tokenAmount));
                wallet.setEthereumBalance(wallet.getEthereumBalance().add(ethAmount));
                walletRepository.save(wallet);

                transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
                transaction.setTransactionHash(receipt.getTransactionHash());
                transaction.setBlockNumber(receipt.getBlockNumber().longValue());
                transaction.setEthereumAmount(ethAmount);
                transactionRepository.save(transaction);

                log.info("Withdrew {} tokens for {} ETH, tx hash: {}",
                        tokenAmount, ethAmount, receipt.getTransactionHash());

                return transaction;
            } else {
                transaction.setStatus(Transaction.TransactionStatus.FAILED);
                transactionRepository.save(transaction);

                log.error("Token withdrawal failed: {}", receipt.getStatus());
                throw new RuntimeException("Token withdrawal failed: " + receipt.getStatus());
            }
        } catch (Exception e) {
            transaction.setStatus(Transaction.TransactionStatus.FAILED);
            transactionRepository.save(transaction);

            log.error("Error withdrawing tokens", e);
            throw new RuntimeException("Failed to withdraw tokens", e);
        }
    }

    @Transactional
    public Transaction depositTokens(Wallet wallet, BigDecimal tokenAmount) throws Exception {
        ensureUserHasTokens(wallet, tokenAmount);

        Credentials userCredentials = getWalletCredentials(wallet);

        Transaction transaction = Transaction.builder()
                .user(wallet.getUser())
                .wallet(wallet)
                .amount(tokenAmount)
                .type(Transaction.TransactionType.DEPOSIT)
                .status(Transaction.TransactionStatus.PENDING)
                .casinoTokenAmount(tokenAmount)
                .transactionTime(LocalDateTime.now())
                .build();

        transactionRepository.save(transaction);

        try {
            CasinoToken userTokenContract = CasinoToken.load(
                    casinoToken.getContractAddress(),
                    web3j,
                    userCredentials,
                    gasProvider
            );

            TransactionReceipt approvalReceipt = userTokenContract.approve(
                    casinoWallet.getContractAddress(),
                    tokenAmount.toBigInteger()
            ).send();

            if (!approvalReceipt.isStatusOK()) {
                transaction.setStatus(Transaction.TransactionStatus.FAILED);
                transactionRepository.save(transaction);
                throw new RuntimeException("Token approval failed");
            }

            Credentials masterCredentials = getMasterWalletCredentials();

            TransactionReceipt receipt = casinoWallet.depositFor(
                    wallet.getEthereumAddress(),
                    tokenAmount.toBigInteger()
            ).send();

            if (receipt.isStatusOK()) {
                transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
                transaction.setTransactionHash(receipt.getTransactionHash());
                transaction.setBlockNumber(receipt.getBlockNumber().longValue());
                transactionRepository.save(transaction);

                log.info("Deposited {} tokens, tx hash: {}",
                        tokenAmount, receipt.getTransactionHash());

                return transaction;
            } else {
                transaction.setStatus(Transaction.TransactionStatus.FAILED);
                transactionRepository.save(transaction);

                log.error("Token deposit failed: {}", receipt.getStatus());
                throw new RuntimeException("Token deposit failed: " + receipt.getStatus());
            }
        } catch (Exception e) {
            transaction.setStatus(Transaction.TransactionStatus.FAILED);
            transactionRepository.save(transaction);

            log.error("Error depositing tokens", e);
            throw new RuntimeException("Failed to deposit tokens: " + e.getMessage(), e);
        }
    }

    private void ensureUserHasTokens(Wallet wallet, BigDecimal requiredAmount) throws Exception {
        BigInteger userTokenBalance = casinoToken.balanceOf(wallet.getEthereumAddress()).send();

        if (new BigDecimal(userTokenBalance).compareTo(requiredAmount) < 0) {
            log.info("User {} doesn't have enough tokens, transferring from master wallet",
                    wallet.getEthereumAddress());

            BigDecimal amountToTransfer = requiredAmount.multiply(BigDecimal.valueOf(1.2));

            TransactionReceipt tokenTransferReceipt = casinoToken.transfer(
                    wallet.getEthereumAddress(),
                    amountToTransfer.toBigInteger()
            ).send();

            if (!tokenTransferReceipt.isStatusOK()) {
                throw new RuntimeException("Token transfer to user failed");
            }

            log.info("Transferred {} tokens to user {}",
                    amountToTransfer, wallet.getEthereumAddress());
        }
    }

    @Transactional
    public Transaction withdrawFromCasino(Wallet wallet, BigDecimal tokenAmount) throws Exception {
        Credentials credentials = getMasterWalletCredentials();

        Transaction transaction = Transaction.builder()
                .user(wallet.getUser())
                .wallet(wallet)
                .amount(tokenAmount)
                .type(Transaction.TransactionType.WITHDRAWAL)
                .status(Transaction.TransactionStatus.PENDING)
                .casinoTokenAmount(tokenAmount)
                .transactionTime(LocalDateTime.now())
                .build();

        transactionRepository.save(transaction);

        try {
            TransactionReceipt receipt = casinoWallet.withdrawFor(
                    wallet.getEthereumAddress(),
                    tokenAmount.toBigInteger()
            ).send();

            if (receipt.isStatusOK()) {
                transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
                transaction.setTransactionHash(receipt.getTransactionHash());
                transaction.setBlockNumber(receipt.getBlockNumber().longValue());
                transactionRepository.save(transaction);

                log.info("Withdrew {} tokens from casino, tx hash: {}",
                        tokenAmount, receipt.getTransactionHash());

                return transaction;
            } else {
                transaction.setStatus(Transaction.TransactionStatus.FAILED);
                transactionRepository.save(transaction);

                log.error("Token withdrawal from casino failed: {}", receipt.getStatus());
                throw new RuntimeException("Token withdrawal from casino failed: " + receipt.getStatus());
            }
        } catch (Exception e) {
            transaction.setStatus(Transaction.TransactionStatus.FAILED);
            transactionRepository.save(transaction);

            log.error("Error withdrawing tokens from casino", e);
            throw new RuntimeException("Failed to withdraw tokens from casino", e);
        }
    }

    @Transactional
    public void syncWalletWithBlockchain(Wallet wallet) throws Exception {
        try {
            EthGetBalance ethBalance = web3j.ethGetBalance(
                    wallet.getEthereumAddress(),
                    DefaultBlockParameterName.LATEST
            ).send();

            BigDecimal ethBalanceInEther = Convert.fromWei(
                    new BigDecimal(ethBalance.getBalance()),
                    Convert.Unit.ETHER
            );

            BigInteger tokenBalance = casinoToken.balanceOf(wallet.getEthereumAddress()).send();
            BigDecimal tokenBalanceDecimal = new BigDecimal(tokenBalance);

            BigDecimal casinoBalanceDecimal = BigDecimal.ZERO;
            if (wallet.isBlockchainRegistered()) {
                try {
                    BigInteger casinoBalance = casinoWallet.getBalance(wallet.getEthereumAddress()).send();
                    casinoBalanceDecimal = new BigDecimal(casinoBalance);
                } catch (Exception e) {
                    log.warn("Could not get casino balance for {}: {}",
                            wallet.getEthereumAddress(), e.getMessage());
                }
            }

            wallet.setEthereumBalance(ethBalanceInEther);
            wallet.setCasinoTokenBalance(tokenBalanceDecimal);
            wallet.setLastSynced(System.currentTimeMillis());

            walletRepository.save(wallet);

            log.info("Synced wallet {}: ETH={}, Tokens={}, Casino={}",
                    wallet.getEthereumAddress(), ethBalanceInEther,
                    tokenBalanceDecimal, casinoBalanceDecimal);
        } catch (Exception e) {
            log.error("Error syncing wallet with blockchain", e);
            throw new RuntimeException("Failed to sync wallet", e);
        }
    }

    public CompletableFuture<org.web3j.tuples.generated.Tuple2<BigInteger, BigInteger>> getUserStats(Wallet wallet) {
        return casinoWallet.getUserStats(wallet.getEthereumAddress()).sendAsync();
    }

    private String encryptPrivateKey(String privateKey) throws Exception {
        if (privateKey.startsWith("0x")) {
            privateKey = privateKey.substring(2);
        }

        Key aesKey = new SecretKeySpec(encryptionKey.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, aesKey);

        byte[] encrypted = cipher.doFinal(privateKey.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }

    private String decryptPrivateKey(String encryptedKey) throws Exception {
        Key aesKey = new SecretKeySpec(encryptionKey.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, aesKey);

        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedKey);
        byte[] decrypted = cipher.doFinal(encryptedBytes);
        String result = new String(decrypted);

        if (!result.startsWith("0x")) {
            result = "0x" + result;
        }

        return result;
    }

    public Credentials getMasterWalletCredentials() {
        return Credentials.create(web3jConfig.getMasterWalletPrivateKey());
    }

    private Credentials getWalletCredentials(Wallet wallet) throws Exception {
        String privateKeyHex = decryptPrivateKey(wallet.getEncryptedKey());
        return Credentials.create(privateKeyHex);
    }
}
