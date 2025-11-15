package hu.bme.aut.crypto_casino_android.ui.presentation.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bme.aut.crypto_casino_android.data.local.WalletKeyManager
import hu.bme.aut.crypto_casino_android.data.model.transaction.BlockchainTransaction
import hu.bme.aut.crypto_casino_android.data.repository.BlockchainTransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BlockchainTransactionsViewModel @Inject constructor(
    transactionRepository: BlockchainTransactionRepository,
    private val walletKeyManager: WalletKeyManager
) : ViewModel() {

    private val _hasWallet = MutableStateFlow(true)
    val hasWallet: StateFlow<Boolean> = _hasWallet.asStateFlow()

    init {
        checkWallet()
    }

    private fun checkWallet() {
        viewModelScope.launch {
            walletKeyManager.getPrimaryWalletAddress.collect { primaryWalletAddress ->
                _hasWallet.value = !primaryWalletAddress.isNullOrBlank()
            }
        }
    }

    val transactions: Flow<PagingData<BlockchainTransaction>> =
        transactionRepository.getTransactions()
            .cachedIn(viewModelScope)
}
