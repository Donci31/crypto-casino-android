package hu.bme.aut.cryptocasino.data.repository

import android.util.Log
import hu.bme.aut.cryptocasino.data.api.RouletteApiService
import hu.bme.aut.cryptocasino.data.model.roulette.RouletteBalanceResponse
import hu.bme.aut.cryptocasino.data.model.roulette.RouletteBetRequest
import hu.bme.aut.cryptocasino.data.model.roulette.RouletteConfigResponse
import hu.bme.aut.cryptocasino.data.model.roulette.RouletteGameCreatedResponse
import hu.bme.aut.cryptocasino.data.model.roulette.RouletteGameRequest
import hu.bme.aut.cryptocasino.data.model.roulette.RouletteGameSettledResponse
import hu.bme.aut.cryptocasino.data.model.roulette.RoulettePrepareGameResponse
import hu.bme.aut.cryptocasino.data.util.ApiResult
import hu.bme.aut.cryptocasino.data.util.safeApiFlow
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RouletteRepository
    @Inject
    constructor(
        private val apiService: RouletteApiService,
    ) {
        fun getRouletteConfig(): Flow<ApiResult<RouletteConfigResponse>> {
            Log.d(TAG, "Fetching roulette game config")
            return safeApiFlow { apiService.getRouletteConfig() }
        }

        fun prepareGame(): Flow<ApiResult<RoulettePrepareGameResponse>> {
            Log.d(TAG, "Preparing roulette game")
            return safeApiFlow { apiService.prepareGame() }
        }

        fun createGame(
            tempGameId: String,
            bets: List<RouletteBetRequest>,
            clientSeed: String,
        ): Flow<ApiResult<RouletteGameCreatedResponse>> {
            Log.d(TAG, "Creating roulette game: tempGameId=$tempGameId, bets=${bets.size}")
            val request =
                RouletteGameRequest(
                    tempGameId = tempGameId,
                    bets = bets,
                    clientSeed = clientSeed,
                )
            return safeApiFlow { apiService.createGame(request) }
        }

        fun settleGame(gameId: Long): Flow<ApiResult<RouletteGameSettledResponse>> {
            Log.d(TAG, "Settling roulette game: gameId=$gameId")
            return safeApiFlow { apiService.settleGame(gameId) }
        }

        fun getBalance(): Flow<ApiResult<RouletteBalanceResponse>> {
            Log.d(TAG, "Fetching vault balance")
            return safeApiFlow { apiService.getBalance() }
        }

        companion object {
            private const val TAG = "RouletteRepository"
        }
    }
