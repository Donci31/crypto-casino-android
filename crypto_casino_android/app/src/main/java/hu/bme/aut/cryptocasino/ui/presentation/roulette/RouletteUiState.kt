package hu.bme.aut.cryptocasino.ui.presentation.roulette

import hu.bme.aut.cryptocasino.data.model.roulette.BetType
import hu.bme.aut.cryptocasino.data.model.roulette.RouletteBetRequest
import java.math.BigDecimal
import java.security.SecureRandom

enum class RouletteGamePhase {
    IDLE,
    COMMITTING,
    COMMITTED_WAITING,
    REVEALING,
    VERIFICATION,
}

data class RouletteUiState(
    val isLoading: Boolean = true,
    val gamePhase: RouletteGamePhase = RouletteGamePhase.IDLE,
    val minBet: BigDecimal = BigDecimal.ZERO,
    val maxBet: BigDecimal = BigDecimal.ZERO,
    val houseEdge: Int = 0,
    val selectedChipValue: BigDecimal = BigDecimal("10"),
    val placedBets: List<PlacedBet> = emptyList(),
    val totalBetAmount: BigDecimal = BigDecimal.ZERO,
    val balance: BigDecimal = BigDecimal.ZERO,
    val currentGameId: Long? = null,
    val serverSeedHash: String? = null,
    val serverSeed: String? = null,
    val transactionHash: String? = null,
    val blockNumber: Long? = null,
    val winningNumber: Int? = null,
    val totalPayout: BigDecimal? = null,
    val error: String? = null,
    val clientSeed: String = generateClientSeed(),
) {
    val isSpinning: Boolean get() = gamePhase == RouletteGamePhase.REVEALING
}

private fun generateClientSeed(): String {
    val random = SecureRandom()
    val bytes = ByteArray(32)
    random.nextBytes(bytes)
    return bytes.joinToString("") { "%02x".format(it) }
}

data class PlacedBet(
    val betType: BetType,
    val amount: BigDecimal,
    val number: Int = 0,
    val displayText: String,
) {
    fun toRequest(): RouletteBetRequest =
        RouletteBetRequest(
            betType = betType,
            amount = amount,
            number = number,
        )
}
