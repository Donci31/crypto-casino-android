package hu.bme.aut.crypto_casino_android.data.model.roulette

data class RouletteGameRequest(
    val bets: List<RouletteBetRequest>,
    val clientSeed: String
)
