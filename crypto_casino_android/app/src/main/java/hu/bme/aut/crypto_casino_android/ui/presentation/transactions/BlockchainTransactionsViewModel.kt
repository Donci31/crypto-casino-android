package hu.bme.aut.crypto_casino_android.ui.presentation.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
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

    val transactions: Flow<PagingData<BlockchainTransaction>> =
        transactionRepository.getTransactions()
            .cachedIn(viewModelScope)

    private val _transactionState = MutableStateFlow<ApiResult<BlockchainTransaction>?>(null)
    val transactionState: StateFlow<ApiResult<BlockchainTransaction>?> = _transactionState

    private val _selectedTransaction = MutableStateFlow<BlockchainTransaction?>(null)
    val selectedTransaction: StateFlow<BlockchainTransaction?> = _selectedTransaction

    fun getTransactionByHash(txHash: String) {
        viewModelScope.launch {
            _transactionState.value = ApiResult.Loading
            transactionRepository.getTransactionByHash(txHash).collect { result ->
                _transactionState.value = result
            }
        }
    }

    fun getTransactionByCompositeKey(txHash: String, blockNumber: Long, logIndex: Int) {
        viewModelScope.launch {
            _transactionState.value = ApiResult.Loading
            transactionRepository.getTransactionByCompositeKey(txHash, blockNumber, logIndex).collect { result ->
                _transactionState.value = result
            }
        }
    }

    fun setSelectedTransaction(transaction: BlockchainTransaction) {
        _selectedTransaction.value = transaction
    }

    fun clearSelectedTransaction() {
        _selectedTransaction.value = null
    }

    fun clearTransactionState() {
        _transactionState.value = null
    }
}
