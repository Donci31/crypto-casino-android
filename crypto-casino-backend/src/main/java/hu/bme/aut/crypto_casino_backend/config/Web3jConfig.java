package hu.bme.aut.crypto_casino_backend.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.casinotoken.CasinoToken;
import org.web3j.casinowallet.CasinoWallet;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.StaticGasProvider;

import java.math.BigInteger;

@Configuration
@Slf4j
@Getter
public class Web3jConfig {

    @Value("${web3j.client-address}")
    private String clientAddress;

    @Value("${web3j.gas-price}")
    private BigInteger gasPrice;

    @Value("${web3j.gas-limit}")
    private BigInteger gasLimit;

    @Value("${web3j.contract.casino-token}")
    private String casinoTokenAddress;

    @Value("${web3j.contract.casino-wallet}")
    private String casinoWalletAddress;

    @Value("${wallet.master-wallet.private-key}")
    private String masterWalletPrivateKey;

    @Value("${wallet.master-wallet.address}")
    private String masterWalletAddress;

    @Bean
    public Web3j web3j() {
        Web3j web3j = Web3j.build(new HttpService(clientAddress));
        log.info("Connected to Ethereum client: {}", clientAddress);
        return web3j;
    }

    @Bean
    public Credentials masterWalletCredentials() {
        return Credentials.create(masterWalletPrivateKey);
    }

    @Bean
    public ContractGasProvider contractGasProvider() {
        return new StaticGasProvider(gasPrice, gasLimit);
    }

    @Bean
    public CasinoToken casinoToken(Web3j web3j, Credentials credentials, ContractGasProvider gasProvider) {
        return CasinoToken.load(
                casinoTokenAddress,
                web3j,
                credentials,
                gasProvider
        );
    }

    @Bean
    public CasinoWallet casinoWallet(Web3j web3j, Credentials credentials, ContractGasProvider gasProvider) {
        return CasinoWallet.load(
                casinoWalletAddress,
                web3j,
                credentials,
                gasProvider
        );
    }
}
