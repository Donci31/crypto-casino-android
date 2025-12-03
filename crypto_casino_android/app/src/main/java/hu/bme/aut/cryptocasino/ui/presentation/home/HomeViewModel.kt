package hu.bme.aut.cryptocasino.ui.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bme.aut.cryptocasino.data.local.WalletKeyManager
import hu.bme.aut.cryptocasino.data.repository.BlockchainTransactionRepository
import hu.bme.aut.cryptocasino.data.repository.StatsRepository
import hu.bme.aut.cryptocasino.data.util.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val statsRepository: StatsRepository,
    private val transactionRepository: BlockchainTransactionRepository,
    private val walletKeyManager: WalletKeyManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        checkWalletAndLoadData()
    }

    private fun checkWalletAndLoadData() {
        viewModelScope.launch {
            walletKeyManager.getPrimaryWalletAddress.collect { primaryWalletAddress ->
                if (primaryWalletAddress.isNullOrBlank()) {
                    _uiState.update { it.copy(hasWallet = false, isLoading = false) }
                } else {
                    _uiState.update { it.copy(hasWallet = true) }
                    loadDashboardData()
                }
            }
        }
    }

    fun loadDashboardData() {
        loadQuickStats()
        loadRecentTransactions()
    }

    private fun loadQuickStats() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            statsRepository.getQuickStats().collect { result ->
                when (result) {
                    is ApiResult.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                quickStats = result.data,
                                error = null
                            )
                        }
                    }
                    is ApiResult.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = result.exception.message
                            )
                        }
                    }
                    is ApiResult.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                }
            }
        }
    }

    private fun loadRecentTransactions() {
        viewModelScope.launch {
            transactionRepository.getRecentTransactions(0, 5).collect { result ->
                when (result) {
                    is ApiResult.Success -> {
                        _uiState.update {
                            it.copy(recentTransactions = result.data.content)
                        }
                    }
                    is ApiResult.Error -> {
                    }
                    is ApiResult.Loading -> {
                    }
                }
            }
        }
    }

    fun refresh() {
        loadDashboardData()
    }
}
