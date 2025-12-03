package hu.bme.aut.cryptocasino.blockchain

import android.util.Log
import hu.bme.aut.cryptocasino.BuildConfig
import hu.bme.aut.cryptocasino.data.model.wallet.WalletData
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
class BlockchainService
    @Inject
    constructor() {
        private val web3j: Web3j by lazy {
            Log.d(TAG, "Initializing Web3j with RPC URL: ${BuildConfig.ETHEREUM_RPC_URL}")
            Web3j.build(HttpService(BuildConfig.ETHEREUM_RPC_URL))
        }

        private val gasProvider: ContractGasProvider = DefaultGasProvider()

        companion object {
            private const val TAG = "BlockchainService"
            const val CASINO_TOKEN_ADDRESS = BuildConfig.CASINO_TOKEN_ADDRESS
            const val CASINO_VAULT_ADDRESS = BuildConfig.CASINO_VAULT_ADDRESS
        }

        suspend fun getEthBalance(walletData: WalletData): BigDecimal =
            withContext(Dispatchers.IO) {
                Log.d(TAG, "Getting ETH balance for: ${walletData.address}")
                val ethBalance = web3j.ethGetBalance(walletData.address, DefaultBlockParameterName.LATEST).send()
                val balance = Convert.fromWei(ethBalance.balance.toString(), Convert.Unit.ETHER)
                Log.d(TAG, "ETH balance: $balance")
                balance
            }

        private fun loadCredentials(privateKey: String): Credentials = Credentials.create(privateKey)

        private fun loadCasinoTokenContract(credentials: Credentials): CasinoToken {
            val transactionManager = RawTransactionManager(web3j, credentials)
            return CasinoToken.load(
                CASINO_TOKEN_ADDRESS,
                web3j,
                transactionManager,
                gasProvider,
            )
        }

        private fun loadCasinoVaultContract(credentials: Credentials): CasinoVault {
            val transactionManager = RawTransactionManager(web3j, credentials)
            return CasinoVault.load(
                CASINO_VAULT_ADDRESS,
                web3j,
                transactionManager,
                gasProvider,
            )
        }

        suspend fun getTokenBalance(walletData: WalletData): BigInteger =
            withContext(Dispatchers.IO) {
                Log.d(TAG, "Getting token balance for: ${walletData.address}")
                val credentials = loadCredentials(walletData.privateKey)
                val tokenContract = loadCasinoTokenContract(credentials)
                val balance = tokenContract.balanceOf(walletData.address).send()
                Log.d(TAG, "Token balance: $balance")
                balance
            }

        suspend fun getVaultBalance(walletData: WalletData): BigInteger =
            withContext(Dispatchers.IO) {
                Log.d(TAG, "Getting vault balance for: ${walletData.address}")
                val credentials = loadCredentials(walletData.privateKey)
                val vaultContract = loadCasinoVaultContract(credentials)
                val balance = vaultContract.balances(walletData.address).send()
                Log.d(TAG, "Vault balance: $balance")
                balance
            }

        suspend fun purchaseTokens(
            walletData: WalletData,
            ethAmount: BigDecimal,
        ): String =
            withContext(Dispatchers.IO) {
                Log.d(TAG, "Purchasing tokens: $ethAmount ETH for ${walletData.address}")
                val credentials = loadCredentials(walletData.privateKey)
                val tokenContract = loadCasinoTokenContract(credentials)

                val weiValue = Convert.toWei(ethAmount, Convert.Unit.ETHER).toBigInteger()
                val tx = tokenContract.purchaseTokens(weiValue)

                val txHash = tx.send().transactionHash
                Log.d(TAG, "Token purchase tx: $txHash")
                txHash
            }

        suspend fun exchangeTokens(
            walletData: WalletData,
            tokenAmount: BigInteger,
        ): String =
            withContext(Dispatchers.IO) {
                Log.d(TAG, "Exchanging tokens: $tokenAmount for ${walletData.address}")
                val credentials = loadCredentials(walletData.privateKey)
                val tokenContract = loadCasinoTokenContract(credentials)

                val tx = tokenContract.exchangeTokens(tokenAmount)
                val txHash = tx.send().transactionHash
                Log.d(TAG, "Token exchange tx: $txHash")
                txHash
            }

        suspend fun depositToVault(
            walletData: WalletData,
            tokenAmount: BigInteger,
        ): String =
            withContext(Dispatchers.IO) {
                Log.d(TAG, "Depositing to vault: $tokenAmount for ${walletData.address}")
                val credentials = loadCredentials(walletData.privateKey)
                val tokenContract = loadCasinoTokenContract(credentials)
                val vaultContract = loadCasinoVaultContract(credentials)

                Log.d(TAG, "Approving token transfer")
                val approveTx = tokenContract.approve(CASINO_VAULT_ADDRESS, tokenAmount).send()
                if (!approveTx.isStatusOK) {
                    Log.e(TAG, "Failed to approve token transfer")
                    throw Exception("Failed to approve token transfer")
                }

                Log.d(TAG, "Executing deposit")
                val depositTx = vaultContract.deposit(tokenAmount)
                val txHash = depositTx.send().transactionHash
                Log.d(TAG, "Deposit tx: $txHash")
                txHash
            }

        suspend fun withdrawFromVault(
            walletData: WalletData,
            tokenAmount: BigInteger,
        ): String =
            withContext(Dispatchers.IO) {
                Log.d(TAG, "Withdrawing from vault: $tokenAmount for ${walletData.address}")
                val credentials = loadCredentials(walletData.privateKey)
                val vaultContract = loadCasinoVaultContract(credentials)

                val tx = vaultContract.withdraw(tokenAmount)
                val txHash = tx.send().transactionHash
                Log.d(TAG, "Withdraw tx: $txHash")
                txHash
            }
    }
