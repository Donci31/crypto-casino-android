package hu.bme.aut.crypto_casino_android.data.repository

import android.util.Log
import hu.bme.aut.crypto_casino_android.data.api.RouletteApiService
import hu.bme.aut.crypto_casino_android.data.model.roulette.RouletteBalanceResponse
import hu.bme.aut.crypto_casino_android.data.model.roulette.RouletteBetRequest
import hu.bme.aut.crypto_casino_android.data.model.roulette.RouletteConfigResponse
import hu.bme.aut.crypto_casino_android.data.model.roulette.RouletteGameCreatedResponse
import hu.bme.aut.crypto_casino_android.data.model.roulette.RouletteGameRequest
import hu.bme.aut.crypto_casino_android.data.model.roulette.RouletteGameSettledResponse
import hu.bme.aut.crypto_casino_android.data.model.roulette.RouletteGameStatusResponse
import hu.bme.aut.crypto_casino_android.data.util.ApiResult
import hu.bme.aut.crypto_casino_android.data.util.safeApiFlow
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RouletteRepository @Inject constructor(
    private val apiService: RouletteApiService
) {

    fun getRouletteConfig(): Flow<ApiResult<RouletteConfigResponse>> {
        Log.d(TAG, "Fetching roulette game config")
        return safeApiFlow { apiService.getRouletteConfig() }
    }

    fun createGame(
        bets: List<RouletteBetRequest>,
        clientSeed: String
    ): Flow<ApiResult<RouletteGameCreatedResponse>> {
        Log.d(TAG, "Creating roulette game: bets=${bets.size}, clientSeed=$clientSeed")
        val request = RouletteGameRequest(
            bets = bets,
            clientSeed = clientSeed
        )
        return safeApiFlow { apiService.createGame(request) }
    }

    fun settleGame(gameId: Long): Flow<ApiResult<RouletteGameSettledResponse>> {
        Log.d(TAG, "Settling roulette game: gameId=$gameId")
        return safeApiFlow { apiService.settleGame(gameId) }
    }

    fun getGameStatus(gameId: Long): Flow<ApiResult<RouletteGameStatusResponse>> {
        Log.d(TAG, "Fetching roulette game status: gameId=$gameId")
        return safeApiFlow { apiService.getGameStatus(gameId) }
    }

    fun getBalance(): Flow<ApiResult<RouletteBalanceResponse>> {
        Log.d(TAG, "Fetching vault balance")
        return safeApiFlow { apiService.getBalance() }
    }

    companion object {
        private const val TAG = "RouletteRepository"
    }
}
