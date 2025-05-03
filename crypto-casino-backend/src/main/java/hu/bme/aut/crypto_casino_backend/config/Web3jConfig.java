package hu.bme.aut.crypto_casino_backend.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.web3j.casinotoken.CasinoToken;
import org.web3j.casinovault.CasinoVault;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.websocket.WebSocketService;
import org.web3j.slotmachine.SlotMachine;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.StaticGasProvider;

import java.math.BigInteger;

@Configuration
@Slf4j
@Getter
public class Web3jConfig {

    @Value("${web3j.rpc-address}")
    private String blockchainRpcUrl;

    @Value("${web3j.ws-address}")
    private String blockchainWsUrl;

    @Value("${web3j.gas-price}")
    private BigInteger gasPrice;

    @Value("${web3j.gas-limit}")
    private BigInteger gasLimit;

    @Value("${web3j.contract.casino-token}")
    private String casinoTokenAddress;

    @Value("${web3j.contract.casino-vault}")
    private String casinoVaultAddress;

    @Value("${web3j.contract.slot-machine}")
    private String slotMachineAddress;

    @Value("${wallet.master-wallet.private-key}")
    private String masterWalletPrivateKey;

    @Value("${wallet.master-wallet.address}")
    private String masterWalletAddress;

    @Bean()
    @Primary
    public Web3j httpWeb3j() {
        return Web3j.build(new HttpService(blockchainRpcUrl));
    }

    @Bean
    @Qualifier("wsWeb3j")
    public Web3j wsWeb3j() throws Exception {
        WebSocketService webSocketService = new WebSocketService(blockchainWsUrl, true);
        webSocketService.connect();
        return Web3j.build(webSocketService);
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
    public CasinoToken casinoToken(@Qualifier("httpWeb3j") Web3j web3j, Credentials credentials, ContractGasProvider gasProvider) {
        return CasinoToken.load(
                casinoTokenAddress,
                web3j,
                credentials,
                gasProvider
        );
    }

    @Bean
    public CasinoVault casinoVault(@Qualifier("httpWeb3j") Web3j web3j, Credentials credentials, ContractGasProvider gasProvider) {
        return CasinoVault.load(
                casinoVaultAddress,
                web3j,
                credentials,
                gasProvider
        );
    }

    @Bean
    public SlotMachine slotMachineContract(
            @Qualifier("httpWeb3j") Web3j web3j,
            Credentials masterWalletCredentials,
            ContractGasProvider gasProvider) {

        log.info("Loading SlotMachine contract at address: {}", slotMachineAddress);

        return SlotMachine.load(
                slotMachineAddress,
                web3j,
                masterWalletCredentials,
                gasProvider
        );
    }
}
