package hu.bme.aut.crypto_casino_android.ui.screens.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hu.bme.aut.crypto_casino_android.data.model.wallet.*
import hu.bme.aut.crypto_casino_android.data.repository.BlockchainRepository
import hu.bme.aut.crypto_casino_android.data.repository.WalletRepository
import hu.bme.aut.crypto_casino_android.data.util.ApiResult
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bme.aut.crypto_casino_android.data.model.blockchain.TokenRate
import hu.bme.aut.crypto_casino_android.data.model.transaction.Transaction
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val walletRepository: WalletRepository,
    private val blockchainRepository: BlockchainRepository
) : ViewModel() {

    private val _walletState = MutableStateFlow<ApiResult<Wallet>?>(null)
    val walletState: StateFlow<ApiResult<Wallet>?> = _walletState

    private val _tokenRateState = MutableStateFlow<ApiResult<TokenRate>?>(null)
    val tokenRateState: StateFlow<ApiResult<TokenRate>?> = _tokenRateState

    private val _transactionState = MutableStateFlow<ApiResult<Transaction>?>(null)
    val transactionState: StateFlow<ApiResult<Transaction>?> = _transactionState

    private val _walletStatsState = MutableStateFlow<ApiResult<UserStats>?>(null)
    val walletStatsState: StateFlow<ApiResult<UserStats>?> = _walletStatsState

    init {
        getWallet()
        getTokenRate()
        getWalletStats()
    }

    fun getWallet() {
        viewModelScope.launch {
            try {
                walletRepository.getWallet()
                    .catch { error ->
                        if (error.message?.contains("wallet not initialized", ignoreCase = true) == true) {
                            initializeWallet()
                        } else {
                            _walletState.value = ApiResult.Error(error)
                        }
                    }
                    .collect { result ->
                        _walletState.value = result
                    }
            } catch (e: Exception) {
                _walletState.value = ApiResult.Error(e)
            }
        }
    }

    private fun initializeWallet() {
        viewModelScope.launch {
            walletRepository.initializeWallet()
                .collect { result ->
                    _walletState.value = result
                }
        }
    }

    fun getTokenRate() {
        viewModelScope.launch {
            blockchainRepository.getTokenRate()
                .collect { result ->
                    _tokenRateState.value = result
                }
        }
    }

    fun getWalletStats() {
        viewModelScope.launch {
            walletRepository.getWalletStats()
                .collect { result ->
                    _walletStatsState.value = result
                }
        }
    }

    fun purchaseTokens(ethAmount: Double, destinationAddress: String? = null) {
        viewModelScope.launch {
            val exchangeRequest = ExchangeRequest(
                ethAmount = ethAmount,
                destinationAddress = destinationAddress
            )
            walletRepository.purchaseTokens(exchangeRequest)
                .collect { result ->
                    _transactionState.value = result
                    if (result is ApiResult.Success) {
                        getWallet()
                    }
                }
        }
    }

    fun withdrawTokens(ethAmount: Double, destinationAddress: String? = null) {
        viewModelScope.launch {
            val exchangeRequest = ExchangeRequest(
                ethAmount = ethAmount,
                destinationAddress = destinationAddress
            )
            walletRepository.withdrawTokens(exchangeRequest)
                .collect { result ->
                    _transactionState.value = result
                    if (result is ApiResult.Success) {
                        getWallet()
                    }
                }
        }
    }

    fun depositTokens(tokenAmount: Double) {
        viewModelScope.launch {
            val request = TokenAmount(tokenAmount = tokenAmount)
            walletRepository.depositTokens(request)
                .collect { result ->
                    _transactionState.value = result
                    if (result is ApiResult.Success) {
                        getWallet()
                    }
                }
        }
    }

    fun withdrawCasinoTokens(tokenAmount: Double) {
        viewModelScope.launch {
            val request = TokenAmount(tokenAmount = tokenAmount)
            walletRepository.withdrawCasinoTokens(request)
                .collect { result ->
                    _transactionState.value = result
                    if (result is ApiResult.Success) {
                        getWallet()
                    }
                }
        }
    }

    fun resetTransactionState() {
        _transactionState.value = null
    }
}
