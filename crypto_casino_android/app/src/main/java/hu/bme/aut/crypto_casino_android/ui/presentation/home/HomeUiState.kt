package hu.bme.aut.crypto_casino_android.ui.presentation.home

import hu.bme.aut.crypto_casino_android.data.model.stats.QuickStatsResponse
import hu.bme.aut.crypto_casino_android.data.model.transaction.BlockchainTransaction

data class HomeUiState(
    val isLoading: Boolean = false,
    val quickStats: QuickStatsResponse? = null,
    val recentTransactions: List<BlockchainTransaction> = emptyList(),
    val error: String? = null,
    val hasWallet: Boolean = true
)
