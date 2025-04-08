package hu.bme.aut.crypto_casino_android.data.repository

import hu.bme.aut.crypto_casino_android.data.api.WalletApi
import hu.bme.aut.crypto_casino_android.data.model.wallet.*
import hu.bme.aut.crypto_casino_android.data.model.transaction.Transaction
import hu.bme.aut.crypto_casino_android.data.util.ApiResult
import hu.bme.aut.crypto_casino_android.data.util.safeApiFlow
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WalletRepository @Inject constructor(
    private val walletApi: WalletApi
) {
    fun initializeWallet(): Flow<ApiResult<Wallet>> =
        safeApiFlow { walletApi.initializeWallet() }

    fun getWallet(): Flow<ApiResult<Wallet>> =
        safeApiFlow { walletApi.getWallet() }

    fun purchaseTokens(exchangeRequest: ExchangeRequest): Flow<ApiResult<Transaction>> =
        safeApiFlow { walletApi.purchaseTokens(exchangeRequest) }

    fun withdrawTokens(exchangeRequest: ExchangeRequest): Flow<ApiResult<Transaction>> =
        safeApiFlow { walletApi.withdrawTokens(exchangeRequest) }

    fun depositTokens(tokenAmount: TokenAmount): Flow<ApiResult<Transaction>> =
        safeApiFlow { walletApi.depositTokens(tokenAmount) }

    fun withdrawCasinoTokens(tokenAmount: TokenAmount): Flow<ApiResult<Transaction>> =
        safeApiFlow { walletApi.withdrawCasinoTokens(tokenAmount) }

    fun getWalletStats(): Flow<ApiResult<UserStats>> =
        safeApiFlow { walletApi.getWalletStats() }
}
