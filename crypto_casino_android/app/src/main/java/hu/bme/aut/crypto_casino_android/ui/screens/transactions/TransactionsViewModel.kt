package hu.bme.aut.crypto_casino_android.ui.screens.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hu.bme.aut.crypto_casino_android.data.model.transaction.Transaction
import hu.bme.aut.crypto_casino_android.data.repository.TransactionRepository
import hu.bme.aut.crypto_casino_android.data.util.ApiResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _transactionsState = MutableStateFlow<ApiResult<List<Transaction>>?>(null)
    val transactionsState: StateFlow<ApiResult<List<Transaction>>?> = _transactionsState

    private val _transactionState = MutableStateFlow<ApiResult<Transaction>?>(null)
    val transactionState: StateFlow<ApiResult<Transaction>?> = _transactionState

    private val _transactionStatsState = MutableStateFlow<ApiResult<Map<String, Long>>?>(null)
    val transactionStatsState: StateFlow<ApiResult<Map<String, Long>>?> = _transactionStatsState

    private var currentPage = 0
    private val pageSize = 10
    private var isLastPage = false

    init {
        getTransactions()
    }

    fun getTransactions(refresh: Boolean = false) {
        if (refresh) {
            currentPage = 0
            isLastPage = false
            _transactionsState.value = null
        }

        if (!isLastPage) {
            viewModelScope.launch {
                transactionRepository.getTransactions(currentPage, pageSize)
                    .collect { result ->
                        when (result) {
                            is ApiResult.Success -> {
                                val newTransactions = result.data
                                if (newTransactions.isEmpty()) {
                                    isLastPage = true
                                } else {
                                    currentPage++

                                    // If we're refreshing or on the first page, replace the list
                                    // Otherwise append to the existing list
                                    val existingTransactions = if (refresh || currentPage == 1) {
                                        emptyList()
                                    } else {
                                        (_transactionsState.value as? ApiResult.Success)?.data ?: emptyList()
                                    }

                                    val combinedList = existingTransactions + newTransactions
                                    _transactionsState.value = ApiResult.Success(combinedList)
                                }
                            }
                            else -> _transactionsState.value = result
                        }
                    }
            }
        }
    }

    fun loadMoreTransactions() {
        if (!isLastPage && _transactionsState.value !is ApiResult.Loading) {
            getTransactions(false)
        }
    }

    fun getTransactionById(id: Long) {
        viewModelScope.launch {
            transactionRepository.getTransactionById(id)
                .collect { result ->
                    _transactionState.value = result
                }
        }
    }
}
