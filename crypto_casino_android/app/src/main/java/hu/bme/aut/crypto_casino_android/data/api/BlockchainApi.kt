package hu.bme.aut.crypto_casino_android.data.api

import hu.bme.aut.crypto_casino_android.data.model.blockchain.NetworkInfo
import hu.bme.aut.crypto_casino_android.data.model.blockchain.TokenRate
import retrofit2.Response
import retrofit2.http.GET

interface BlockchainApi {
    @GET("blockchain/network-info")
    suspend fun getNetworkInfo(): Response<NetworkInfo>

    @GET("blockchain/token-rate")
    suspend fun getTokenRate(): Response<TokenRate>
}
