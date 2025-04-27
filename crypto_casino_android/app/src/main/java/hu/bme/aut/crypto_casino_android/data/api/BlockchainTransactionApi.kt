
package hu.bme.aut.crypto_casino_android.data.api

import hu.bme.aut.crypto_casino_android.data.model.transaction.BlockchainTransaction
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface BlockchainTransactionApi {
    @GET("/api/transactions")
    suspend fun getMyTransactions(): Response<List<BlockchainTransaction>>

    @GET("/api/transactions/{txHash}")
    suspend fun getTransactionByHash(@Path("txHash") txHash: String): Response<BlockchainTransaction>
}
