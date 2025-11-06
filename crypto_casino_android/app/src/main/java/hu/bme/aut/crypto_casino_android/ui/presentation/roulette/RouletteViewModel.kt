package hu.bme.aut.crypto_casino_android.ui.presentation.roulette

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bme.aut.crypto_casino_android.data.model.roulette.BetType
import hu.bme.aut.crypto_casino_android.data.repository.RouletteRepository
import hu.bme.aut.crypto_casino_android.data.util.ApiResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class RouletteViewModel @Inject constructor(
    private val rouletteRepository: RouletteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RouletteUiState())
    val uiState: StateFlow<RouletteUiState> = _uiState.asStateFlow()

    init {
        loadRouletteConfig()
    }

    fun loadRouletteConfig() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            rouletteRepository.getRouletteConfig().collect { result ->
                when (result) {
                    is ApiResult.Success -> {
                        val config = result.data
                        _uiState.update { currentState ->
                            currentState.copy(
                                minBet = config.minBet,
                                maxBet = config.maxBet,
                                houseEdge = config.houseEdge,
                                selectedChipValue = config.minBet,
                                isLoading = false
                            )
                        }
                        loadBalance()
                    }
                    is ApiResult.Error -> {
                        _uiState.update {
                            it.copy(
                                error = "Failed to load roulette configuration: ${result.exception.message}",
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

    fun selectChipValue(value: BigDecimal) {
        _uiState.update { it.copy(selectedChipValue = value) }
    }

    fun placeBet(betType: BetType, number: Int = 0) {
        val state = _uiState.value
        val chipValue = state.selectedChipValue

        if (chipValue < state.minBet) {
            _uiState.update { it.copy(error = "Chip value below minimum bet") }
            return
        }

        if (chipValue > state.maxBet) {
            _uiState.update { it.copy(error = "Chip value above maximum bet") }
            return
        }

        val existingBets = state.placedBets
        if (existingBets.size >= 20) {
            _uiState.update { it.copy(error = "Maximum 20 bets allowed") }
            return
        }

        val displayText = when (betType) {
            BetType.STRAIGHT -> "Straight $number"
            BetType.RED -> "Red"
            BetType.BLACK -> "Black"
            BetType.ODD -> "Odd"
            BetType.EVEN -> "Even"
            BetType.LOW -> "Low (1-18)"
            BetType.HIGH -> "High (19-36)"
            BetType.DOZEN_FIRST -> "1st Dozen"
            BetType.DOZEN_SECOND -> "2nd Dozen"
            BetType.DOZEN_THIRD -> "3rd Dozen"
            BetType.COLUMN_FIRST -> "1st Column"
            BetType.COLUMN_SECOND -> "2nd Column"
            BetType.COLUMN_THIRD -> "3rd Column"
        }

        val newBet = PlacedBet(
            betType = betType,
            amount = chipValue,
            number = number,
            displayText = displayText
        )

        val newTotalBet = state.totalBetAmount + chipValue

        if (newTotalBet > state.balance) {
            _uiState.update { it.copy(error = "Insufficient balance") }
            return
        }

        _uiState.update {
            it.copy(
                placedBets = existingBets + newBet,
                totalBetAmount = newTotalBet,
                error = null
            )
        }
    }

    fun removeBet(index: Int) {
        val state = _uiState.value
        if (index < 0 || index >= state.placedBets.size) return

        val removedBet = state.placedBets[index]
        val newBets = state.placedBets.toMutableList().apply { removeAt(index) }
        val newTotalBet = state.totalBetAmount - removedBet.amount

        _uiState.update {
            it.copy(
                placedBets = newBets,
                totalBetAmount = newTotalBet
            )
        }
    }

    fun clearAllBets() {
        _uiState.update {
            it.copy(
                placedBets = emptyList(),
                totalBetAmount = BigDecimal.ZERO
            )
        }
    }

    fun spin() {
        val state = _uiState.value

        if (state.gamePhase != RouletteGamePhase.IDLE) {
            return
        }

        if (state.placedBets.isEmpty()) {
            _uiState.update { it.copy(error = "Place at least one bet to spin") }
            return
        }

        if (state.balance < state.totalBetAmount) {
            _uiState.update { it.copy(error = "Insufficient balance") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(gamePhase = RouletteGamePhase.COMMITTING, error = null, winningNumber = null) }

            val betRequests = state.placedBets.map { it.toRequest() }

            rouletteRepository.createGame(
                bets = betRequests,
                clientSeed = state.clientSeed
            ).collect { result ->
                when (result) {
                    is ApiResult.Success -> {
                        val createdGame = result.data
                        _uiState.update { currentState ->
                            currentState.copy(
                                gamePhase = RouletteGamePhase.COMMITTED_WAITING,
                                currentGameId = createdGame.gameId,
                                serverSeedHash = createdGame.serverSeedHash,
                                transactionHash = createdGame.transactionHash,
                                blockNumber = createdGame.blockNumber
                            )
                        }
                    }
                    is ApiResult.Error -> {
                        _uiState.update {
                            it.copy(
                                error = "Failed to create game: ${result.exception.message}",
                                gamePhase = RouletteGamePhase.IDLE
                            )
                        }
                    }
                    is ApiResult.Loading -> {
                    }
                }
            }
        }
    }

    fun proceedToReveal() {
        val state = _uiState.value
        if (state.gamePhase == RouletteGamePhase.COMMITTED_WAITING && state.currentGameId != null) {
            settleGame(state.currentGameId)
        }
    }

    private fun settleGame(gameId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(gamePhase = RouletteGamePhase.REVEALING) }

            rouletteRepository.settleGame(gameId).collect { result ->
                when (result) {
                    is ApiResult.Success -> {
                        val settledGame = result.data
                        _uiState.update { currentState ->
                            currentState.copy(
                                gamePhase = RouletteGamePhase.VERIFICATION,
                                winningNumber = settledGame.winningNumber,
                                totalPayout = settledGame.totalPayout,
                                serverSeed = settledGame.serverSeed,
                                currentGameId = null
                            )
                        }
                        loadBalance()
                    }
                    is ApiResult.Error -> {
                        _uiState.update {
                            it.copy(
                                error = "Failed to settle game: ${result.exception.message}",
                                gamePhase = RouletteGamePhase.IDLE,
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

    fun continueAfterVerification() {
        _uiState.update { it.copy(gamePhase = RouletteGamePhase.REVEALED) }
    }

    private fun loadBalance() {
        viewModelScope.launch {
            rouletteRepository.getBalance().collect { result ->
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
        _uiState.update {
            it.copy(
                gamePhase = RouletteGamePhase.IDLE,
                winningNumber = null,
                totalPayout = null,
                error = null,
                placedBets = emptyList(),
                totalBetAmount = BigDecimal.ZERO
            )
        }
    }
}
