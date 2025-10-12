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
    /**
     * Returns a Flow of PagingData for infinite scroll transactions list.
     *
     * PagingConfig parameters:
     * - pageSize: Number of items to load per page
     * - enablePlaceholders: false to avoid showing null placeholders while loading
     * - initialLoadSize: Number of items to load on initial load (same as pageSize for consistency)
     * - prefetchDistance: How many items before the end should trigger the next page load
     */
    fun getTransactions(): Flow<PagingData<BlockchainTransaction>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                initialLoadSize = 20,
                prefetchDistance = 5
            ),
            pagingSourceFactory = { BlockchainTransactionPagingSource(api) }
        ).flow
    }

    fun getTransactionByHash(txHash: String): Flow<ApiResult<BlockchainTransaction>> {
        return safeApiFlow { api.getTransactionByHash(txHash) }
    }
}
