package hu.bme.aut.crypto_casino_android.ui.presentation.roulette

import hu.bme.aut.crypto_casino_android.data.model.roulette.BetType
import hu.bme.aut.crypto_casino_android.data.model.roulette.RouletteBetRequest
import java.math.BigDecimal
import java.security.SecureRandom

data class RouletteUiState(
    val isLoading: Boolean = true,
    val isCreatingGame: Boolean = false,
    val isSettling: Boolean = false,
    val isSpinning: Boolean = false,
    val minBet: BigDecimal = BigDecimal.ZERO,
    val maxBet: BigDecimal = BigDecimal.ZERO,
    val houseEdge: Int = 0,
    val selectedChipValue: BigDecimal = BigDecimal("10"),
    val placedBets: List<PlacedBet> = emptyList(),
    val totalBetAmount: BigDecimal = BigDecimal.ZERO,
    val balance: BigDecimal = BigDecimal.ZERO,
    val currentGameId: Long? = null,
    val serverSeedHash: String? = null,
    val winningNumber: Int? = null,
    val totalPayout: BigDecimal? = null,
    val error: String? = null,
    val clientSeed: String = generateClientSeed()
)

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
    val displayText: String
) {
    fun toRequest(): RouletteBetRequest = RouletteBetRequest(
        betType = betType,
        amount = amount,
        number = number
    )
}
