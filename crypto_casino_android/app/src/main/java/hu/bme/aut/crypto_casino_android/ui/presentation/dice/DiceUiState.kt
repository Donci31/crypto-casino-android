package hu.bme.aut.crypto_casino_android.ui.presentation.dice

import hu.bme.aut.crypto_casino_android.data.model.dice.BetType
import java.math.BigDecimal
import java.security.SecureRandom

data class DiceUiState(
    val isLoading: Boolean = true,
    val isCreatingGame: Boolean = false,
    val isSettling: Boolean = false,
    val minBet: BigDecimal = BigDecimal.ZERO,
    val maxBet: BigDecimal = BigDecimal.ZERO,
    val houseEdge: Int = 0,
    val currentBet: BigDecimal = BigDecimal.ZERO,
    val prediction: Int = 50,
    val betType: BetType = BetType.ROLL_UNDER,
    val balance: BigDecimal = BigDecimal.ZERO,
    val currentGameId: Long? = null,
    val serverSeedHash: String? = null,
    val result: Int? = null,
    val payout: BigDecimal? = null,
    val won: Boolean? = null,
    val error: String? = null,
    val clientSeed: String = generateClientSeed()
)

private fun generateClientSeed(): String {
    val random = SecureRandom()
    val bytes = ByteArray(32)
    random.nextBytes(bytes)
    return bytes.joinToString("") { "%02x".format(it) }
}
