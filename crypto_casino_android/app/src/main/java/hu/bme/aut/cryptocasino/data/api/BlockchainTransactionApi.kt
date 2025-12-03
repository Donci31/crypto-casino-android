package hu.bme.aut.cryptocasino.data.api

import hu.bme.aut.cryptocasino.data.model.transaction.BlockchainTransaction
import hu.bme.aut.cryptocasino.data.model.transaction.PagedResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface BlockchainTransactionApi {
    @GET("/api/transactions")
    suspend fun getMyTransactions(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
    ): Response<PagedResponse<BlockchainTransaction>>

    @GET("/api/transactions/{txHash}")
    suspend fun getTransactionByHash(
        @Path("txHash") txHash: String,
    ): Response<BlockchainTransaction>

    @GET("/api/transactions/{txHash}/{blockNumber}/{logIndex}")
    suspend fun getTransactionByCompositeKey(
        @Path("txHash") txHash: String,
        @Path("blockNumber") blockNumber: Long,
        @Path("logIndex") logIndex: Int,
    ): Response<BlockchainTransaction>
}
