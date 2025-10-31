package hu.bme.aut.crypto_casino_android.ui.presentation.dice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bme.aut.crypto_casino_android.data.model.dice.BetType
import hu.bme.aut.crypto_casino_android.data.repository.DiceRepository
import hu.bme.aut.crypto_casino_android.data.util.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class DiceViewModel @Inject constructor(
    private val diceRepository: DiceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiceUiState())
    val uiState: StateFlow<DiceUiState> = _uiState.asStateFlow()

    init {
        loadDiceConfig()
    }

    fun loadDiceConfig() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            diceRepository.getDiceConfig().collect { result ->
                when (result) {
                    is ApiResult.Success -> {
                        val config = result.data
                        _uiState.update { currentState ->
                            currentState.copy(
                                minBet = config.minBet,
                                maxBet = config.maxBet,
                                houseEdge = config.houseEdge,
                                currentBet = config.minBet,
                                isLoading = false
                            )
                        }
                        loadBalance()
                    }
                    is ApiResult.Error -> {
                        _uiState.update {
                            it.copy(
                                error = "Failed to load dice configuration: ${result.exception.message}",
                                isLoading = false
                            )
                        }
                    }
                    is ApiResult.Loading -> {
                    }
                }
            }
        }
    }

    fun increaseBet() {
        _uiState.update { currentState ->
            val increment = BigDecimal("10")
            val newBet = currentState.currentBet.add(increment)
            val finalBet = if (newBet > currentState.maxBet) currentState.maxBet else newBet
            currentState.copy(currentBet = finalBet)
        }
    }

    fun decreaseBet() {
        _uiState.update { currentState ->
            val decrement = BigDecimal("10")
            val newBet = currentState.currentBet.subtract(decrement)
            val finalBet = if (newBet < currentState.minBet) currentState.minBet else newBet
            currentState.copy(currentBet = finalBet)
        }
    }

    fun setPrediction(prediction: Int) {
        _uiState.update { it.copy(prediction = prediction) }
    }

    fun setBetType(betType: BetType) {
        _uiState.update { it.copy(betType = betType) }
    }

    fun playGame() {
        val state = _uiState.value

        if (state.currentGameId != null && !state.isSettling) {
            return
        }

        if (state.balance < state.currentBet) {
            _uiState.update { it.copy(error = "Insufficient balance") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isCreatingGame = true, error = null, result = null, won = null) }

            diceRepository.createGame(
                betAmount = state.currentBet,
                prediction = state.prediction,
                betType = state.betType,
                clientSeed = state.clientSeed
            ).collect { result ->
                when (result) {
                    is ApiResult.Success -> {
                        val createdGame = result.data
                        _uiState.update { currentState ->
                            currentState.copy(
                                isCreatingGame = false,
                                currentGameId = createdGame.gameId,
                                serverSeedHash = createdGame.serverSeedHash
                            )
                        }
                        settleGame(createdGame.gameId)
                    }
                    is ApiResult.Error -> {
                        _uiState.update {
                            it.copy(
                                error = "Failed to create game: ${result.exception.message}",
                                isCreatingGame = false
                            )
                        }
                    }
                    is ApiResult.Loading -> {
                    }
                }
            }
        }
    }

    private fun settleGame(gameId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSettling = true) }

            diceRepository.settleGame(gameId).collect { result ->
                when (result) {
                    is ApiResult.Success -> {
                        val settledGame = result.data
                        _uiState.update { currentState ->
                            currentState.copy(
                                isSettling = false,
                                result = settledGame.result,
                                payout = settledGame.payout,
                                won = settledGame.won,
                                currentGameId = null
                            )
                        }
                        loadBalance()
                    }
                    is ApiResult.Error -> {
                        _uiState.update {
                            it.copy(
                                error = "Failed to settle game: ${result.exception.message}",
                                isSettling = false,
                                currentGameId = null
                            )
                        }
                    }
                    is ApiResult.Loading -> {
                    }
                }
            }
        }
    }

    private fun loadBalance() {
        viewModelScope.launch {
            diceRepository.getBalance().collect { result ->
                when (result) {
                    is ApiResult.Success -> {
                        _uiState.update { it.copy(balance = result.data.balance) }
                    }
                    is ApiResult.Error -> {
                    }
                    is ApiResult.Loading -> {
                    }
                }
            }
        }
    }

    fun clearResult() {
        _uiState.update { it.copy(result = null, won = null, payout = null, error = null) }
    }
}
