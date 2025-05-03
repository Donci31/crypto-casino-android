package hu.bme.aut.crypto_casino_android.ui.presentation.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bme.aut.crypto_casino_android.blockchain.BlockchainService
import hu.bme.aut.crypto_casino_android.data.model.wallet.WalletData
import hu.bme.aut.crypto_casino_android.data.repository.WalletRepository
import hu.bme.aut.crypto_casino_android.data.util.ApiResult
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.web3j.crypto.Credentials
import org.web3j.crypto.Keys
import org.web3j.utils.Numeric
import java.math.BigDecimal
import java.math.BigInteger
import javax.inject.Inject

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val walletRepository: WalletRepository,
    private val blockchainService: BlockchainService
) : ViewModel() {

    private val _walletUiState = MutableStateFlow<WalletUiState>(WalletUiState.Loading)
    val walletUiState: StateFlow<WalletUiState> = _walletUiState

    private val _walletOperationState = MutableStateFlow<WalletOperationState>(WalletOperationState.Idle)
    val walletOperationState: StateFlow<WalletOperationState> = _walletOperationState

    private val _activeWallet = MutableStateFlow<WalletData?>(null)
    val activeWallet: StateFlow<WalletData?> = _activeWallet

    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage: SharedFlow<String> = _errorMessage

    private val _successMessage = MutableSharedFlow<String>()
    val successMessage: SharedFlow<String> = _successMessage

    private val _ethBalance = MutableStateFlow<BigDecimal>(BigDecimal.ZERO)
    val ethBalance: StateFlow<BigDecimal> = _ethBalance

    private val _tokenBalance = MutableStateFlow<BigInteger>(BigInteger.ZERO)
    val tokenBalance: StateFlow<BigInteger> = _tokenBalance

    private val _vaultBalance = MutableStateFlow<BigInteger>(BigInteger.ZERO)
    val vaultBalance: StateFlow<BigInteger> = _vaultBalance

    init {
        loadWallets()
    }

    fun loadWallets() {
        viewModelScope.launch {
            _walletUiState.value = WalletUiState.Loading

            walletRepository.getAllWalletData()
                .catch { e ->
                    _walletUiState.value = WalletUiState.Error(e.message ?: "Unknown error")
                }
                .collect { wallets ->
                    if (wallets.isEmpty()) {
                        _walletUiState.value = WalletUiState.Empty
                    } else {
                        _walletUiState.value = WalletUiState.Success(wallets)

                        _activeWallet.value = wallets.find { it.isPrimary } ?: wallets.first()

                        loadBalances()
                    }
                }
        }
    }

    fun loadBalances() {
        viewModelScope.launch {
            _activeWallet.value?.let { wallet ->
                try {
                    _ethBalance.value = blockchainService.getEthBalance(wallet)
                    _tokenBalance.value = blockchainService.getTokenBalance(wallet)
                    _vaultBalance.value = blockchainService.getVaultBalance(wallet)
                } catch (e: Exception) {
                    _errorMessage.emit("Failed to load balances: ${e.message}")
                }
            }
        }
    }

    fun setActiveWallet(wallet: WalletData) {
        viewModelScope.launch {
            _activeWallet.value = wallet
            loadBalances()
        }
    }

    fun setPrimaryWallet(wallet: WalletData) {
        viewModelScope.launch {
            _walletOperationState.value = WalletOperationState.Loading
            wallet.id?.let { walletId ->
                walletRepository.setPrimaryWallet(walletId)
                    .collect { result ->
                        when (result) {
                            is ApiResult.Success -> {
                                _successMessage.emit("Primary wallet updated")
                                walletRepository.saveWalletWithKey(
                                    wallet.address,
                                    wallet.privateKey,
                                    true
                                )
                                loadWallets()
                            }
                            is ApiResult.Error -> {
                                _errorMessage.emit("Failed to set primary wallet: ${result.exception.message}")
                                _walletOperationState.value = WalletOperationState.Error(result.exception.message ?: "Unknown error")
                            }
                            is ApiResult.Loading -> {
                            }
                        }
                    }
            } ?: run {
                _errorMessage.emit("Wallet not synced with server")
                _walletOperationState.value = WalletOperationState.Error("Wallet not synced with server")
            }
        }
    }

    fun addWallet(privateKey: String, label: String, isPrimary: Boolean = false) {
        viewModelScope.launch {
            _walletOperationState.value = WalletOperationState.Loading
            try {
                val credentials = Credentials.create(privateKey)
                val address = credentials.address

                walletRepository.addWallet(address, label, isPrimary)
                    .collect { result ->
                        when (result) {
                            is ApiResult.Success -> {
                                walletRepository.saveWalletWithKey(address, privateKey, isPrimary)
                                _successMessage.emit("Wallet added successfully")
                                _walletOperationState.value = WalletOperationState.Success
                                loadWallets()
                            }
                            is ApiResult.Error -> {
                                _errorMessage.emit("Failed to add wallet: ${result.exception.message}")
                                _walletOperationState.value = WalletOperationState.Error(result.exception.message ?: "Unknown error")
                            }
                            is ApiResult.Loading -> {
                            }
                        }
                    }
            } catch (e: Exception) {
                _errorMessage.emit("Invalid private key: ${e.message}")
                _walletOperationState.value = WalletOperationState.Error("Invalid private key: ${e.message}")
            }
        }
    }

    fun generateNewWallet(label: String, isPrimary: Boolean = false) {
        viewModelScope.launch {
            _walletOperationState.value = WalletOperationState.Loading
            try {
                val ecKeyPair = Keys.createEcKeyPair()
                val privateKey = Numeric.toHexStringWithPrefix(ecKeyPair.privateKey)
                val credentials = Credentials.create(ecKeyPair)
                val address = credentials.address

                walletRepository.addWallet(address, label, isPrimary)
                    .collect { result ->
                        when (result) {
                            is ApiResult.Success -> {
                                walletRepository.saveWalletWithKey(address, privateKey, isPrimary)
                                _successMessage.emit("New wallet generated successfully")
                                _walletOperationState.value = WalletOperationState.Success
                                loadWallets()
                            }
                            is ApiResult.Error -> {
                                _errorMessage.emit("Failed to add wallet: ${result.exception.message}")
                                _walletOperationState.value = WalletOperationState.Error(result.exception.message ?: "Unknown error")
                            }
                            is ApiResult.Loading -> {
                            }
                        }
                    }
            } catch (e: Exception) {
                _errorMessage.emit("Failed to generate wallet: ${e.message}")
                _walletOperationState.value = WalletOperationState.Error("Failed to generate wallet: ${e.message}")
            }
        }
    }

    fun purchaseTokens(ethAmount: BigDecimal) {
        viewModelScope.launch {
            _activeWallet.value?.let { wallet ->
                _walletOperationState.value = WalletOperationState.Loading
                try {
                    val txHash = blockchainService.purchaseTokens(wallet, ethAmount)
                    _successMessage.emit("Tokens purchased successfully. Transaction hash: $txHash")
                    _walletOperationState.value = WalletOperationState.Success
                    kotlinx.coroutines.delay(2000)
                    loadBalances()
                } catch (e: Exception) {
                    _errorMessage.emit("Failed to purchase tokens: ${e.message}")
                    _walletOperationState.value = WalletOperationState.Error(e.message ?: "Unknown error")
                }
            } ?: run {
                _errorMessage.emit("No active wallet selected")
            }
        }
    }

    fun exchangeTokens(tokenAmount: BigInteger) {
        viewModelScope.launch {
            _activeWallet.value?.let { wallet ->
                _walletOperationState.value = WalletOperationState.Loading
                try {
                    val txHash = blockchainService.exchangeTokens(wallet, tokenAmount)
                    _successMessage.emit("Tokens exchanged successfully. Transaction hash: $txHash")
                    _walletOperationState.value = WalletOperationState.Success
                    kotlinx.coroutines.delay(2000)
                    loadBalances()
                } catch (e: Exception) {
                    _errorMessage.emit("Failed to exchange tokens: ${e.message}")
                    _walletOperationState.value = WalletOperationState.Error(e.message ?: "Unknown error")
                }
            } ?: run {
                _errorMessage.emit("No active wallet selected")
            }
        }
    }

    fun depositToVault(tokenAmount: BigInteger) {
        viewModelScope.launch {
            _activeWallet.value?.let { wallet ->
                _walletOperationState.value = WalletOperationState.Loading
                try {
                    val txHash = blockchainService.depositToVault(wallet, tokenAmount)
                    _successMessage.emit("Tokens deposited to vault successfully. Transaction hash: $txHash")
                    _walletOperationState.value = WalletOperationState.Success
                    kotlinx.coroutines.delay(2000)
                    loadBalances()
                } catch (e: Exception) {
                    _errorMessage.emit("Failed to deposit tokens: ${e.message}")
                    _walletOperationState.value = WalletOperationState.Error(e.message ?: "Unknown error")
                }
            } ?: run {
                _errorMessage.emit("No active wallet selected")
            }
        }
    }

    fun withdrawFromVault(tokenAmount: BigInteger) {
        viewModelScope.launch {
            _activeWallet.value?.let { wallet ->
                _walletOperationState.value = WalletOperationState.Loading
                try {
                    val txHash = blockchainService.withdrawFromVault(wallet, tokenAmount)
                    _successMessage.emit("Tokens withdrawn from vault successfully. Transaction hash: $txHash")
                    _walletOperationState.value = WalletOperationState.Success
                    kotlinx.coroutines.delay(2000)
                    loadBalances()
                } catch (e: Exception) {
                    _errorMessage.emit("Failed to withdraw tokens: ${e.message}")
                    _walletOperationState.value = WalletOperationState.Error(e.message ?: "Unknown error")
                }
            } ?: run {
                _errorMessage.emit("No active wallet selected")
            }
        }
    }

    sealed class WalletUiState {
        object Loading : WalletUiState()
        object Empty : WalletUiState()
        data class Success(val wallets: List<WalletData>) : WalletUiState()
        data class Error(val message: String) : WalletUiState()
    }

    sealed class WalletOperationState {
        object Idle : WalletOperationState()
        object Loading : WalletOperationState()
        object Success : WalletOperationState()
        data class Error(val message: String) : WalletOperationState()
    }
}
