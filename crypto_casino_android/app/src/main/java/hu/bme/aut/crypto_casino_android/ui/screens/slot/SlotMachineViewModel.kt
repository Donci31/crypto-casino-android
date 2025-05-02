package hu.bme.aut.crypto_casino_android.ui.screens.slot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bme.aut.crypto_casino_android.data.model.slot.GameHistoryResponse
import hu.bme.aut.crypto_casino_android.data.model.slot.SpinResponse
import hu.bme.aut.crypto_casino_android.data.repository.SlotMachineRepository
import hu.bme.aut.crypto_casino_android.data.util.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class SlotMachineViewModel @Inject constructor(
    private val slotMachineRepository: SlotMachineRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SlotMachineUiState())
    val uiState: StateFlow<SlotMachineUiState> = _uiState.asStateFlow()

    init {
        loadSlotConfig()
    }

    fun loadSlotConfig() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            slotMachineRepository.getSlotConfig().collect { result ->
                when (result) {
                    is ApiResult.Success -> {
                        val config = result.data
                        _uiState.update { currentState ->
                            currentState.copy(
                                minBet = config.minBet,
                                maxBet = config.maxBet,
                                currentBet = config.minBet,
                                isLoading = false
                            )
                        }
                        loadBalance()
                        loadGameHistory()
                    }
                    is ApiResult.Error -> {
                        _uiState.update {
                            it.copy(
                                error = "Failed to load slot configuration: ${result.exception.message}",
                                isLoading = false
                            )
                        }
                    }
                    is ApiResult.Loading -> {
                        // Loading state is already set before collection starts
                    }
                }
            }
        }
    }

    fun increaseBet() {
        _uiState.update { currentState ->
            val newBet = currentState.currentBet.add(BigDecimal("0.1"))
            val finalBet = if (newBet > currentState.maxBet) currentState.maxBet else newBet
            currentState.copy(currentBet = finalBet)
        }
    }

    fun decreaseBet() {
        _uiState.update { currentState ->
            val newBet = currentState.currentBet.subtract(BigDecimal("0.1"))
            val finalBet = if (newBet < currentState.minBet) currentState.minBet else newBet
            currentState.copy(currentBet = finalBet)
        }
    }

    fun spin() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSpinning = true, error = null) }

            slotMachineRepository.spin(_uiState.value.currentBet).collect { result ->
                when (result) {
                    is ApiResult.Success -> {
                        val spinResult = result.data
                        _uiState.update { currentState ->
                            currentState.copy(
                                isSpinning = false,
                                lastSpin = spinResult,
                                reels = spinResult.reels,
                                spinCount = currentState.spinCount + 1,
                                selectedTab = 0 // Stay on slot machine tab
                            )
                        }
                        loadBalance()
                        loadGameHistory()
                    }
                    is ApiResult.Error -> {
                        _uiState.update {
                            it.copy(
                                isSpinning = false,
                                error = "Failed to execute spin: ${result.exception.message}"
                            )
                        }
                    }
                    is ApiResult.Loading -> {
                        // Loading state is already set before collection starts
                    }
                }
            }
        }
    }

    fun setSelectedTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    fun refreshAll() {
        loadBalance()
        loadGameHistory()
    }

    private fun loadBalance() {
        viewModelScope.launch {
            slotMachineRepository.getBalance().collect { result ->
                when (result) {
                    is ApiResult.Success -> {
                        _uiState.update { it.copy(balance = result.data) }
                    }
                    is ApiResult.Error -> {
                        _uiState.update {
                            it.copy(error = "Failed to load balance: ${result.exception.message}")
                        }
                    }
                    is ApiResult.Loading -> {
                        // Don't show loading for balance updates to avoid UI flicker
                    }
                }
            }
        }
    }

    private fun loadGameHistory() {
        viewModelScope.launch {
            slotMachineRepository.getGameHistory().collect { result ->
                when (result) {
                    is ApiResult.Success -> {
                        _uiState.update { it.copy(gameHistory = result.data) }
                    }
                    is ApiResult.Error -> {
                        _uiState.update {
                            it.copy(error = "Failed to load game history: ${result.exception.message}")
                        }
                    }
                    is ApiResult.Loading -> {
                        // Don't show loading for history updates to avoid UI flicker
                    }
                }
            }
        }
    }
}
