package hu.bme.aut.crypto_casino_android.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import hu.bme.aut.crypto_casino_android.data.api.BlockchainTransactionApi
import hu.bme.aut.crypto_casino_android.data.model.transaction.BlockchainTransaction
import hu.bme.aut.crypto_casino_android.data.paging.BlockchainTransactionPagingSource
import hu.bme.aut.crypto_casino_android.data.util.ApiResult
import hu.bme.aut.crypto_casino_android.data.util.safeApiFlow
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BlockchainTransactionRepository @Inject constructor(
    private val api: BlockchainTransactionApi
) {
    fun getTransactions(pageSize: Int = 20): Flow<PagingData<BlockchainTransaction>> {
        return Pager(
            config = PagingConfig(
                pageSize = pageSize,
                enablePlaceholders = false,
                initialLoadSize = pageSize
            ),
            pagingSourceFactory = { BlockchainTransactionPagingSource(api) }
        ).flow
    }

    fun getTransactionByHash(txHash: String): Flow<ApiResult<BlockchainTransaction>> {
        return safeApiFlow { api.getTransactionByHash(txHash) }
    }
}
