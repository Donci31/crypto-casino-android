package hu.bme.aut.cryptocasino.data.repository

import android.util.Log
import hu.bme.aut.cryptocasino.data.api.DiceApiService
import hu.bme.aut.cryptocasino.data.model.dice.BetType
import hu.bme.aut.cryptocasino.data.model.dice.DiceBalanceResponse
import hu.bme.aut.cryptocasino.data.model.dice.DiceConfigResponse
import hu.bme.aut.cryptocasino.data.model.dice.DiceGameCreatedResponse
import hu.bme.aut.cryptocasino.data.model.dice.DiceGameRequest
import hu.bme.aut.cryptocasino.data.model.dice.DiceGameSettledResponse
import hu.bme.aut.cryptocasino.data.model.dice.DicePrepareGameResponse
import hu.bme.aut.cryptocasino.data.util.ApiResult
import hu.bme.aut.cryptocasino.data.util.safeApiFlow
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiceRepository
    @Inject
    constructor(
        private val apiService: DiceApiService,
    ) {
        fun getDiceConfig(): Flow<ApiResult<DiceConfigResponse>> {
            Log.d(TAG, "Fetching dice game config")
            return safeApiFlow { apiService.getDiceConfig() }
        }

        fun prepareGame(): Flow<ApiResult<DicePrepareGameResponse>> {
            Log.d(TAG, "Preparing dice game")
            return safeApiFlow { apiService.prepareGame() }
        }

        fun createGame(
            tempGameId: String,
            betAmount: BigDecimal,
            prediction: Int,
            betType: BetType,
            clientSeed: String,
        ): Flow<ApiResult<DiceGameCreatedResponse>> {
            Log.d(TAG, "Creating dice game: tempGameId=$tempGameId, bet=$betAmount, prediction=$prediction, betType=$betType")
            val request =
                DiceGameRequest(
                    tempGameId = tempGameId,
                    betAmount = betAmount,
                    prediction = prediction,
                    betType = betType,
                    clientSeed = clientSeed,
                )
            return safeApiFlow { apiService.createGame(request) }
        }

        fun settleGame(gameId: Long): Flow<ApiResult<DiceGameSettledResponse>> {
            Log.d(TAG, "Settling dice game: gameId=$gameId")
            return safeApiFlow { apiService.settleGame(gameId) }
        }

        fun getBalance(): Flow<ApiResult<DiceBalanceResponse>> {
            Log.d(TAG, "Fetching vault balance")
            return safeApiFlow { apiService.getBalance() }
        }

        companion object {
            private const val TAG = "DiceRepository"
        }
    }
