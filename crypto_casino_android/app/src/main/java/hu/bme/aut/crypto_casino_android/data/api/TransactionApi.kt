package hu.bme.aut.crypto_casino_android.data.api

import hu.bme.aut.crypto_casino_android.data.model.transaction.Transaction
import retrofit2.Response
import retrofit2.http.*

interface TransactionApi {
    @GET("transactions")
    suspend fun getTransactions(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Response<List<Transaction>>

    @GET("transactions/{id}")
    suspend fun getTransactionById(@Path("id") id: Long): Response<Transaction>
}
