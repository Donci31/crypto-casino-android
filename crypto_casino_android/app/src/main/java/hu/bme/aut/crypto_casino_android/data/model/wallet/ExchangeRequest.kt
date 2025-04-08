package hu.bme.aut.crypto_casino_android.data.model.wallet

data class ExchangeRequest(
    val ethAmount: Double,
    val destinationAddress: String? = null
)
