package hu.bme.aut.crypto_casino_android.data.repository

import hu.bme.aut.crypto_casino_android.data.api.BlockchainTransactionApi
import hu.bme.aut.crypto_casino_android.data.model.transaction.BlockchainTransaction
import hu.bme.aut.crypto_casino_android.data.util.ApiResult
import hu.bme.aut.crypto_casino_android.data.util.safeApiFlow
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BlockchainTransactionRepository @Inject constructor(
    private val api: BlockchainTransactionApi
) {
    fun getMyTransactions(): Flow<ApiResult<List<BlockchainTransaction>>> {
        return safeApiFlow { api.getMyTransactions() }
    }

    fun getTransactionByHash(txHash: String): Flow<ApiResult<BlockchainTransaction>> {
        return safeApiFlow { api.getTransactionByHash(txHash) }
    }
}
