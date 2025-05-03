package hu.bme.aut.crypto_casino_android.ui.presentation.slot

import hu.bme.aut.crypto_casino_android.data.model.slot.GameHistoryResponse
import hu.bme.aut.crypto_casino_android.data.model.slot.SpinResponse
import java.math.BigDecimal

data class SlotMachineUiState(
    val isLoading: Boolean = true,
    val isSpinning: Boolean = false,
    val minBet: BigDecimal = BigDecimal.ZERO,
    val maxBet: BigDecimal = BigDecimal.ZERO,
    val currentBet: BigDecimal = BigDecimal.ZERO,
    val balance: BigDecimal = BigDecimal.ZERO,
    val reels: List<Int> = listOf(0, 0, 0),
    val lastSpin: SpinResponse? = null,
    val gameHistory: List<GameHistoryResponse> = emptyList(),
    val spinCount: Int = 0,
    val error: String? = null,
    val selectedTab: Int = 0 // 0 for slot machine, 1 for history
)
