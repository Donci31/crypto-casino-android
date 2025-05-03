package hu.bme.aut.crypto_casino_android.blockchain

import hu.bme.aut.crypto_casino_android.BuildConfig
import hu.bme.aut.crypto_casino_android.data.model.wallet.WalletData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.http.HttpService
import org.web3j.tx.RawTransactionManager
import org.web3j.tx.gas.ContractGasProvider
import org.web3j.tx.gas.DefaultGasProvider
import org.web3j.utils.Convert
import java.math.BigDecimal
import java.math.BigInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BlockchainService @Inject constructor() {

    private val web3j: Web3j by lazy {
        Web3j.build(HttpService(BuildConfig.ETHEREUM_RPC_URL))
    }

    private val gasProvider: ContractGasProvider = DefaultGasProvider()

    companion object {
        const val CASINO_TOKEN_ADDRESS = BuildConfig.CASINO_TOKEN_ADDRESS
        const val CASINO_VAULT_ADDRESS = BuildConfig.CASINO_VAULT_ADDRESS
    }

    suspend fun getEthBalance(walletData: WalletData): BigDecimal = withContext(Dispatchers.IO) {
        val ethBalance = web3j.ethGetBalance(walletData.address, DefaultBlockParameterName.LATEST).send()
        Convert.fromWei(ethBalance.balance.toString(), Convert.Unit.ETHER)
    }

    private fun loadCredentials(privateKey: String): Credentials {
        return Credentials.create(privateKey)
    }

    private fun loadCasinoTokenContract(credentials: Credentials): CasinoToken {
        val transactionManager = RawTransactionManager(web3j, credentials)
        return CasinoToken.load(
            CASINO_TOKEN_ADDRESS,
            web3j,
            transactionManager,
            gasProvider
        )
    }

    private fun loadCasinoVaultContract(credentials: Credentials): CasinoVault {
        val transactionManager = RawTransactionManager(web3j, credentials)
        return CasinoVault.load(
            CASINO_VAULT_ADDRESS,
            web3j,
            transactionManager,
            gasProvider
        )
    }

    suspend fun getTokenBalance(walletData: WalletData): BigInteger = withContext(Dispatchers.IO) {
        val credentials = loadCredentials(walletData.privateKey)
        val tokenContract = loadCasinoTokenContract(credentials)
        tokenContract.balanceOf(walletData.address).send()
    }

    suspend fun getVaultBalance(walletData: WalletData): BigInteger = withContext(Dispatchers.IO) {
        val credentials = loadCredentials(walletData.privateKey)
        val vaultContract = loadCasinoVaultContract(credentials)
        vaultContract.balances(walletData.address).send()
    }

    suspend fun purchaseTokens(walletData: WalletData, ethAmount: BigDecimal): String = withContext(Dispatchers.IO) {
        val credentials = loadCredentials(walletData.privateKey)
        val tokenContract = loadCasinoTokenContract(credentials)

        val weiValue = Convert.toWei(ethAmount, Convert.Unit.ETHER).toBigInteger()
        val tx = tokenContract.purchaseTokens(weiValue)

        tx.send().transactionHash
    }

    suspend fun exchangeTokens(walletData: WalletData, tokenAmount: BigInteger): String = withContext(Dispatchers.IO) {
        val credentials = loadCredentials(walletData.privateKey)
        val tokenContract = loadCasinoTokenContract(credentials)

        val tx = tokenContract.exchangeTokens(tokenAmount)
        tx.send().transactionHash
    }

    suspend fun depositToVault(walletData: WalletData, tokenAmount: BigInteger): String = withContext(Dispatchers.IO) {
        val credentials = loadCredentials(walletData.privateKey)
        val tokenContract = loadCasinoTokenContract(credentials)
        val vaultContract = loadCasinoVaultContract(credentials)

        val approveTx = tokenContract.approve(CASINO_VAULT_ADDRESS, tokenAmount).send()
        if (!approveTx.isStatusOK) {
            throw Exception("Failed to approve token transfer")
        }

        val depositTx = vaultContract.deposit(tokenAmount)
        depositTx.send().transactionHash
    }

    suspend fun withdrawFromVault(walletData: WalletData, tokenAmount: BigInteger): String = withContext(Dispatchers.IO) {
        val credentials = loadCredentials(walletData.privateKey)
        val vaultContract = loadCasinoVaultContract(credentials)

        val tx = vaultContract.withdraw(tokenAmount)
        tx.send().transactionHash
    }
}
