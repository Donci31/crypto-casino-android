package hu.bme.aut.crypto_casino_android.data.model.dice

data class DiceGameCreatedResponse(
    val gameId: Long,
    val serverSeedHash: String,
    val transactionHash: String? = null,
    val blockNumber: Long? = null
)
