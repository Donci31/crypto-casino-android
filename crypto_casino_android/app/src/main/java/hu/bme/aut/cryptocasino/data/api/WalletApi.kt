package hu.bme.aut.cryptocasino.data.api

import hu.bme.aut.cryptocasino.data.model.wallet.SetPrimaryRequest
import hu.bme.aut.cryptocasino.data.model.wallet.WalletRequest
import hu.bme.aut.cryptocasino.data.model.wallet.WalletResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

interface WalletApi {
    @POST("/api/wallets")
    suspend fun addWallet(
        @Body request: WalletRequest,
    ): Response<WalletResponse>

    @GET("/api/wallets")
    suspend fun getUserWallets(): Response<List<WalletResponse>>

    @PUT("/api/wallets/primary")
    suspend fun setPrimaryWallet(
        @Body request: SetPrimaryRequest,
    ): Response<WalletResponse>
}
