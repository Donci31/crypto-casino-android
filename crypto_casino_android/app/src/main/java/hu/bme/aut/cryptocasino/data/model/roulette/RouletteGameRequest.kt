package hu.bme.aut.cryptocasino.data.model.roulette

data class RouletteGameRequest(
    val tempGameId: String,
    val bets: List<RouletteBetRequest>,
    val clientSeed: String,
)
