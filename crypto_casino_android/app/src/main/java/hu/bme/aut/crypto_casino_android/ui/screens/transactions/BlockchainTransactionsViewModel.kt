package hu.bme.aut.crypto_casino_android.ui.screens.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hu.bme.aut.crypto_casino_android.data.model.transaction.BlockchainTransaction
import hu.bme.aut.crypto_casino_android.data.repository.BlockchainTransactionRepository
import hu.bme.aut.crypto_casino_android.data.util.ApiResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BlockchainTransactionsViewModel @Inject constructor(
    private val transactionRepository: BlockchainTransactionRepository
) : ViewModel() {

    private val _transactionsState = MutableStateFlow<ApiResult<List<BlockchainTransaction>>?>(null)
    val transactionsState: StateFlow<ApiResult<List<BlockchainTransaction>>?> = _transactionsState

    private val _transactionState = MutableStateFlow<ApiResult<BlockchainTransaction>?>(null)
    val transactionState: StateFlow<ApiResult<BlockchainTransaction>?> = _transactionState

    init {
        getTransactions()
    }

    fun getTransactions() {
        viewModelScope.launch {
            _transactionsState.value = ApiResult.Loading
            transactionRepository.getMyTransactions().collect { result ->
                _transactionsState.value = result
            }
        }
    }

    fun getTransactionByHash(txHash: String) {
        viewModelScope.launch {
            _transactionState.value = ApiResult.Loading
            transactionRepository.getTransactionByHash(txHash).collect { result ->
                _transactionState.value = result
            }
        }
    }
}
