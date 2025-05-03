package hu.bme.aut.crypto_casino_android.data.repository

import hu.bme.aut.crypto_casino_android.data.api.WalletApi
import hu.bme.aut.crypto_casino_android.data.local.WalletKeyManager
import hu.bme.aut.crypto_casino_android.data.model.wallet.BalanceResponse
import hu.bme.aut.crypto_casino_android.data.model.wallet.SetPrimaryRequest
import hu.bme.aut.crypto_casino_android.data.model.wallet.WalletData
import hu.bme.aut.crypto_casino_android.data.model.wallet.WalletRequest
import hu.bme.aut.crypto_casino_android.data.model.wallet.WalletResponse
import hu.bme.aut.crypto_casino_android.data.util.ApiResult
import hu.bme.aut.crypto_casino_android.data.util.safeApiFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WalletRepository @Inject constructor(
    private val walletApiService: WalletApi,
    private val walletKeyManager: WalletKeyManager
) {
    fun addWallet(address: String, label: String, isPrimary: Boolean = false): Flow<ApiResult<WalletResponse>> =
        safeApiFlow { walletApiService.addWallet(WalletRequest(address, label, isPrimary)) }

    fun getUserWallets(): Flow<ApiResult<List<WalletResponse>>> =
        safeApiFlow { walletApiService.getUserWallets() }

    fun setPrimaryWallet(walletId: Long): Flow<ApiResult<WalletResponse>> =
        safeApiFlow { walletApiService.setPrimaryWallet(SetPrimaryRequest(walletId)) }

    fun getWalletBalance(walletId: Long): Flow<ApiResult<BalanceResponse>> =
        safeApiFlow { walletApiService.getWalletBalance(walletId) }

    suspend fun saveWalletWithKey(address: String, privateKey: String, isPrimary: Boolean = false) {
        walletKeyManager.saveWalletKey(address, privateKey)

        if (isPrimary) {
            walletKeyManager.setPrimaryWallet(address)
        }
    }

    fun getAllWalletData(): Flow<List<WalletData>> {
        return combine(
            walletKeyManager.getAllWalletKeys,
            getUserWallets().map { apiResult ->
                if (apiResult is ApiResult.Success) apiResult.data else emptyList()
            }
        ) { localKeys, apiWallets ->
            localKeys.map { (address, privateKey) ->
                val apiWallet = apiWallets.find { it.address == address }
                WalletData(
                    address = address,
                    privateKey = privateKey,
                    label = apiWallet?.label ?: (address.take(8) + "..."),
                    isPrimary = apiWallet?.isPrimary == true,
                    id = apiWallet?.id
                )
            }
        }
    }

    fun getPrimaryWalletData(): Flow<WalletData?> {
        return getAllWalletData().map { wallets ->
            wallets.find { it.isPrimary }
        }
    }

    suspend fun deleteWallet(address: String) {
        walletKeyManager.deleteWalletKey(address)
    }

    suspend fun clearAllWalletData() {
        walletKeyManager.clearAllWalletKeys()
    }
}
