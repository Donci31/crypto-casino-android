package hu.bme.aut.cryptocasino.ui.presentation.dice

import hu.bme.aut.cryptocasino.data.model.dice.BetType
import java.math.BigDecimal
import java.security.SecureRandom

enum class DiceGamePhase {
    IDLE,
    COMMITTING,
    COMMITTED_WAITING,
    REVEALING,
    VERIFICATION,
    REVEALED,
}

data class DiceUiState(
    val isLoading: Boolean = true,
    val gamePhase: DiceGamePhase = DiceGamePhase.IDLE,
    val minBet: BigDecimal = BigDecimal.ZERO,
    val maxBet: BigDecimal = BigDecimal.ZERO,
    val houseEdge: Int = 0,
    val currentBet: BigDecimal = BigDecimal.ZERO,
    val prediction: Int = 50,
    val betType: BetType = BetType.ROLL_UNDER,
    val balance: BigDecimal = BigDecimal.ZERO,
    val currentGameId: Long? = null,
    val serverSeedHash: String? = null,
    val serverSeed: String? = null,
    val transactionHash: String? = null,
    val blockNumber: Long? = null,
    val result: Int? = null,
    val payout: BigDecimal? = null,
    val won: Boolean? = null,
    val error: String? = null,
    val clientSeed: String = generateClientSeed(),
) {
    val isCreatingGame: Boolean get() = gamePhase == DiceGamePhase.COMMITTING
    val isSettling: Boolean get() = gamePhase == DiceGamePhase.REVEALING
}

private fun generateClientSeed(): String {
    val random = SecureRandom()
    val bytes = ByteArray(32)
    random.nextBytes(bytes)
    return bytes.joinToString("") { "%02x".format(it) }
}
