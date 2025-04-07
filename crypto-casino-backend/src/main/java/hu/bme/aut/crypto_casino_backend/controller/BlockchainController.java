package hu.bme.aut.crypto_casino_backend.controller;

import hu.bme.aut.crypto_casino_backend.config.Web3jConfig;
import hu.bme.aut.crypto_casino_backend.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.protocol.core.methods.response.NetVersion;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/blockchain")
@RequiredArgsConstructor
public class BlockchainController {

    private final Web3j web3j;
    private final Web3jConfig web3jConfig;

    
    @GetMapping("/network-info")
    public ResponseEntity<Map<String, Object>> getNetworkInfo() throws ExecutionException, InterruptedException {
        Map<String, Object> response = new HashMap<>();

        
        Web3ClientVersion clientVersion = web3j.web3ClientVersion().sendAsync().get();
        response.put("clientVersion", clientVersion.getWeb3ClientVersion());

        
        NetVersion netVersion = web3j.netVersion().sendAsync().get();
        response.put("networkId", netVersion.getNetVersion());

        
        EthBlock latestBlock = web3j.ethGetBlockByNumber(DefaultBlockParameterName.LATEST, false)
                .sendAsync().get();
        response.put("latestBlockNumber", latestBlock.getBlock().getNumber());
        response.put("latestBlockTimestamp", latestBlock.getBlock().getTimestamp());

        
        EthGasPrice gasPrice = web3j.ethGasPrice().sendAsync().get();
        BigDecimal gasPriceInGwei = Convert.fromWei(
                new BigDecimal(gasPrice.getGasPrice()),
                Convert.Unit.GWEI
        );
        response.put("gasPrice", gasPriceInGwei.toString() + " Gwei");

        
        response.put("casinoTokenAddress", web3jConfig.getCasinoTokenAddress());
        response.put("casinoWalletAddress", web3jConfig.getCasinoWalletAddress());

        return ResponseEntity.ok(response);
    }

    
    @GetMapping("/token-rate")
    public ResponseEntity<Map<String, Object>> getTokenRate() {
        Map<String, Object> response = new HashMap<>();

        
        response.put("ethToCstRate", "1:100");
        response.put("cstToEthRate", "100:1");

        return ResponseEntity.ok(response);
    }
}
