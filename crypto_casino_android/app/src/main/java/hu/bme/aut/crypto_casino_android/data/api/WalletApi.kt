package hu.bme.aut.crypto_casino_android.data.api

import hu.bme.aut.crypto_casino_android.data.model.transaction.Transaction
import hu.bme.aut.crypto_casino_android.data.model.wallet.*
import retrofit2.Response
import retrofit2.http.*

interface WalletApi {
    @POST("wallet/initialize")
    suspend fun initializeWallet(): Response<Wallet>

    @GET("wallet")
    suspend fun getWallet(): Response<Wallet>

    @POST("wallet/purchase-tokens")
    suspend fun purchaseTokens(@Body exchangeRequest: ExchangeRequest): Response<Transaction>

    @POST("wallet/withdraw-tokens")
    suspend fun withdrawTokens(@Body exchangeRequest: ExchangeRequest): Response<Transaction>

    @POST("wallet/deposit")
    suspend fun depositTokens(@Body tokenAmount: TokenAmount): Response<Transaction>

    @POST("wallet/withdraw")
    suspend fun withdrawCasinoTokens(@Body tokenAmount: TokenAmount): Response<Transaction>

    @GET("wallet/stats")
    suspend fun getWalletStats(): Response<UserStats>
}
