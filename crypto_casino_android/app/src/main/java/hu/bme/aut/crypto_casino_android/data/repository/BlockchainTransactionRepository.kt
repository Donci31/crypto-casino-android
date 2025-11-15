package hu.bme.aut.crypto_casino_android.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import hu.bme.aut.crypto_casino_android.data.api.BlockchainTransactionApi
import hu.bme.aut.crypto_casino_android.data.model.transaction.BlockchainTransaction
import hu.bme.aut.crypto_casino_android.data.paging.BlockchainTransactionPagingSource
import hu.bme.aut.crypto_casino_android.data.util.safeApiFlow
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BlockchainTransactionRepository @Inject constructor(
    private val api: BlockchainTransactionApi
) {
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

    fun getRecentTransactions(page: Int = 0, size: Int = 5) = safeApiFlow {
        api.getMyTransactions(page, size)
    }
}
