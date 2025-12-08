package hu.bme.aut.cryptocasino.data.model.dice

data class DicePrepareGameResponse(
    val tempGameId: String,
    val serverSeedHash: String,
)
