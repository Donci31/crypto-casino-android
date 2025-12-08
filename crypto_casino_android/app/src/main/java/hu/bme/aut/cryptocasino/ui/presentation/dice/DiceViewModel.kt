package hu.bme.aut.cryptocasino.ui.presentation.dice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bme.aut.cryptocasino.data.model.dice.BetType
import hu.bme.aut.cryptocasino.data.repository.DiceRepository
import hu.bme.aut.cryptocasino.data.util.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class DiceViewModel
    @Inject
    constructor(
        private val diceRepository: DiceRepository,
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
                                    isLoading = false,
                                )
                            }
                            loadBalance()
                        }

                        is ApiResult.Error -> {
                            _uiState.update {
                                it.copy(
                                    error = "Failed to load dice configuration: ${result.exception.message}",
                                    isLoading = false,
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

            if (state.gamePhase != DiceGamePhase.IDLE) {
                return
            }

            if (state.balance < state.currentBet) {
                _uiState.update { it.copy(error = "Insufficient balance") }
                return
            }

            viewModelScope.launch {
                _uiState.update { it.copy(gamePhase = DiceGamePhase.PREPARING, error = null, result = null, won = null) }

                diceRepository.prepareGame().collect { prepareResult ->
                    when (prepareResult) {
                        is ApiResult.Success -> {
                            val prepareData = prepareResult.data
                            _uiState.update { currentState ->
                                currentState.copy(
                                    tempGameId = prepareData.tempGameId,
                                    serverSeedHash = prepareData.serverSeedHash,
                                )
                            }
                        }

                        is ApiResult.Error -> {
                            _uiState.update {
                                it.copy(
                                    error = "Failed to prepare game: ${prepareResult.exception.message}",
                                    gamePhase = DiceGamePhase.IDLE,
                                )
                            }
                        }

                        is ApiResult.Loading -> {
                        }
                    }
                }
            }
        }

        fun proceedToCommit() {
            val state = _uiState.value

            if (state.gamePhase != DiceGamePhase.PREPARING || state.tempGameId == null) {
                return
            }

            viewModelScope.launch {
                _uiState.update { it.copy(gamePhase = DiceGamePhase.COMMITTING, error = null) }

                val paddedSeed = padClientSeed(state.clientSeed)
                val tempGameId = state.tempGameId

                diceRepository
                    .createGame(
                        tempGameId = tempGameId,
                        betAmount = state.currentBet,
                        prediction = state.prediction,
                        betType = state.betType,
                        clientSeed = paddedSeed,
                    ).collect { createResult ->
                        when (createResult) {
                            is ApiResult.Success -> {
                                val createdGame = createResult.data
                                _uiState.update { currentState ->
                                    currentState.copy(
                                        gamePhase = DiceGamePhase.COMMITTED_WAITING,
                                        currentGameId = createdGame.gameId,
                                        transactionHash = createdGame.transactionHash,
                                        blockNumber = createdGame.blockNumber,
                                    )
                                }
                            }

                            is ApiResult.Error -> {
                                _uiState.update {
                                    it.copy(
                                        error = "Failed to create game: ${createResult.exception.message}",
                                        gamePhase = DiceGamePhase.IDLE,
                                        tempGameId = null,
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
            if (state.gamePhase == DiceGamePhase.COMMITTED_WAITING && state.currentGameId != null) {
                settleGame(state.currentGameId)
            }
        }

        private fun settleGame(gameId: Long) {
            viewModelScope.launch {
                _uiState.update { it.copy(gamePhase = DiceGamePhase.REVEALING) }

                diceRepository.settleGame(gameId).collect { result ->
                    when (result) {
                        is ApiResult.Success -> {
                            val settledGame = result.data
                            _uiState.update { currentState ->
                                currentState.copy(
                                    gamePhase = DiceGamePhase.VERIFICATION,
                                    result = settledGame.result,
                                    payout = settledGame.payout,
                                    won = settledGame.won,
                                    serverSeed = settledGame.serverSeed,
                                    currentGameId = null,
                                )
                            }
                            loadBalance()
                        }

                        is ApiResult.Error -> {
                            _uiState.update {
                                it.copy(
                                    error = "Failed to settle game: ${result.exception.message}",
                                    gamePhase = DiceGamePhase.IDLE,
                                    currentGameId = null,
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
            _uiState.update { it.copy(gamePhase = DiceGamePhase.REVEALED) }
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
            _uiState.update {
                it.copy(
                    gamePhase = DiceGamePhase.IDLE,
                    result = null,
                    won = null,
                    payout = null,
                    error = null,
                    tempGameId = null,
                )
            }
        }

        fun updateClientSeed(input: String) {
            val sanitized = input.filter { it.isLetterOrDigit() }
            _uiState.update { it.copy(clientSeed = sanitized) }
        }

        fun generateNewClientSeed() {
            _uiState.update { it.copy(clientSeed = generateClientSeed()) }
        }

        private fun generateClientSeed(): String {
            val random = java.security.SecureRandom()
            val bytes = ByteArray(32)
            random.nextBytes(bytes)
            return bytes.joinToString("") { "%02x".format(it) }
        }

        private fun padClientSeed(seed: String): String {
            val targetLength = 64
            return if (seed.length >= targetLength) {
                seed.take(targetLength)
            } else {
                seed.padEnd(targetLength, '0')
            }
        }
    }
