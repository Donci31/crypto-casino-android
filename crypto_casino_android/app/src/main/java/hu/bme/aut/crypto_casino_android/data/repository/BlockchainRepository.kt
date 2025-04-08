package hu.bme.aut.crypto_casino_android.data.repository

import hu.bme.aut.crypto_casino_android.data.api.BlockchainApi
import hu.bme.aut.crypto_casino_android.data.model.blockchain.NetworkInfo
import hu.bme.aut.crypto_casino_android.data.model.blockchain.TokenRate
import hu.bme.aut.crypto_casino_android.data.util.ApiResult
import hu.bme.aut.crypto_casino_android.data.util.safeApiFlow
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BlockchainRepository @Inject constructor(
    private val blockchainApi: BlockchainApi
) {
    fun getNetworkInfo(): Flow<ApiResult<NetworkInfo>> =
        safeApiFlow { blockchainApi.getNetworkInfo() }

    fun getTokenRate(): Flow<ApiResult<TokenRate>> =
        safeApiFlow { blockchainApi.getTokenRate() }
}
