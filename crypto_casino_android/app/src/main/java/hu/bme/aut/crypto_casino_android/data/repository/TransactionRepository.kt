package hu.bme.aut.crypto_casino_android.data.repository

import hu.bme.aut.crypto_casino_android.data.api.TransactionApi
import hu.bme.aut.crypto_casino_android.data.model.transaction.Transaction
import hu.bme.aut.crypto_casino_android.data.util.ApiResult
import hu.bme.aut.crypto_casino_android.data.util.safeApiFlow
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor(
    private val transactionApi: TransactionApi
) {
    fun getTransactions(page: Int = 0, size: Int = 10): Flow<ApiResult<List<Transaction>>> =
        safeApiFlow { transactionApi.getTransactions(page, size) }

    fun getTransactionById(id: Long): Flow<ApiResult<Transaction>> =
        safeApiFlow { transactionApi.getTransactionById(id) }
}
