package hu.bme.aut.crypto_casino_android.data.repository

import android.util.Log
import hu.bme.aut.crypto_casino_android.data.api.SlotMachineApiService
import hu.bme.aut.crypto_casino_android.data.model.slot.GameHistoryResponse
import hu.bme.aut.crypto_casino_android.data.model.slot.SlotConfigResponse
import hu.bme.aut.crypto_casino_android.data.model.slot.SpinRequest
import hu.bme.aut.crypto_casino_android.data.model.slot.SpinResponse
import hu.bme.aut.crypto_casino_android.data.util.ApiResult
import hu.bme.aut.crypto_casino_android.data.util.safeApiFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SlotMachineRepository @Inject constructor(
    private val apiService: SlotMachineApiService,
) {

    fun getSlotConfig(): Flow<ApiResult<SlotConfigResponse>> {
        Log.d(TAG, "Fetching slot machine config")
        return safeApiFlow { apiService.getSlotConfig() }
    }

    fun spin(betAmount: BigDecimal): Flow<ApiResult<SpinResponse>> {
        Log.d(TAG, "Spinning slot machine with bet: $betAmount")
        val request = SpinRequest(betAmount = betAmount)
        return safeApiFlow { apiService.spin(request) }
    }

    fun getGameHistory(): Flow<ApiResult<List<GameHistoryResponse>>> {
        Log.d(TAG, "Fetching game history")
        return safeApiFlow { apiService.getGameHistory() }
    }

    fun getBalance(): Flow<ApiResult<BigDecimal>> {
        Log.d(TAG, "Fetching balance")
        return safeApiFlow { apiService.getBalance() }.mapSuccess { response ->
            response.balance
        }
    }

    private fun <T, R> Flow<ApiResult<T>>.mapSuccess(transform: suspend (T) -> R): Flow<ApiResult<R>> = flow {
        collect { result ->
            emit(
                when (result) {
                    is ApiResult.Success -> ApiResult.Success(transform(result.data))
                    is ApiResult.Error -> ApiResult.Error(result.exception)
                    is ApiResult.Loading -> ApiResult.Loading
                }
            )
        }
    }

    companion object {
        private const val TAG = "SlotMachineRepository"
    }
}
