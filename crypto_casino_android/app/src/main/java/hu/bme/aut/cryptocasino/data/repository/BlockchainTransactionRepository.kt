package hu.bme.aut.cryptocasino.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import hu.bme.aut.cryptocasino.data.api.BlockchainTransactionApi
import hu.bme.aut.cryptocasino.data.model.transaction.BlockchainTransaction
import hu.bme.aut.cryptocasino.data.paging.BlockchainTransactionPagingSource
import hu.bme.aut.cryptocasino.data.util.safeApiFlow
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BlockchainTransactionRepository
    @Inject
    constructor(
        private val api: BlockchainTransactionApi,
    ) {
        fun getTransactions(): Flow<PagingData<BlockchainTransaction>> =
            Pager(
                config =
                    PagingConfig(
                        pageSize = 20,
                        enablePlaceholders = false,
                        initialLoadSize = 20,
                        prefetchDistance = 5,
                    ),
                pagingSourceFactory = { BlockchainTransactionPagingSource(api) },
            ).flow

        fun getRecentTransactions(
            page: Int = 0,
            size: Int = 5,
        ) = safeApiFlow {
            api.getMyTransactions(page, size)
        }
    }
