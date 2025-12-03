package hu.bme.aut.cryptocasino.data.model.wallet

data class WalletRequest(
    val address: String,
    val label: String,
    val isPrimary: Boolean = false,
)
