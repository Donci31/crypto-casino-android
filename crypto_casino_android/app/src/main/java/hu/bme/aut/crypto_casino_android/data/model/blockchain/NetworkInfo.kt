package hu.bme.aut.crypto_casino_android.data.model.blockchain

data class NetworkInfo(
    val clientVersion: String,
    val networkId: String,
    val latestBlockNumber: Int,
    val latestBlockTimestamp: Int,
    val gasPrice: String,
    val casinoTokenAddress: String,
    val casinoWalletAddress: String
)
