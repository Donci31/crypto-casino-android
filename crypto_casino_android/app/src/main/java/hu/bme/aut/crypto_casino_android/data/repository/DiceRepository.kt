package hu.bme.aut.crypto_casino_android.data.repository

import android.util.Log
import hu.bme.aut.crypto_casino_android.data.api.DiceApiService
import hu.bme.aut.crypto_casino_android.data.model.dice.BetType
import hu.bme.aut.crypto_casino_android.data.model.dice.DiceBalanceResponse
import hu.bme.aut.crypto_casino_android.data.model.dice.DiceConfigResponse
import hu.bme.aut.crypto_casino_android.data.model.dice.DiceGameCreatedResponse
import hu.bme.aut.crypto_casino_android.data.model.dice.DiceGameRequest
import hu.bme.aut.crypto_casino_android.data.model.dice.DiceGameSettledResponse
import hu.bme.aut.crypto_casino_android.data.model.dice.DiceGameStatusResponse
import hu.bme.aut.crypto_casino_android.data.util.ApiResult
import hu.bme.aut.crypto_casino_android.data.util.safeApiFlow
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiceRepository @Inject constructor(
    private val apiService: DiceApiService
) {

    fun getDiceConfig(): Flow<ApiResult<DiceConfigResponse>> {
        Log.d(TAG, "Fetching dice game config")
        return safeApiFlow { apiService.getDiceConfig() }
    }

    fun createGame(
        betAmount: BigDecimal,
        prediction: Int,
        betType: BetType,
        clientSeed: String
    ): Flow<ApiResult<DiceGameCreatedResponse>> {
        Log.d(TAG, "Creating dice game: bet=$betAmount, prediction=$prediction, betType=$betType")
        val request = DiceGameRequest(
            betAmount = betAmount,
            prediction = prediction,
            betType = betType,
            clientSeed = clientSeed
        )
        return safeApiFlow { apiService.createGame(request) }
    }

    fun settleGame(gameId: Long): Flow<ApiResult<DiceGameSettledResponse>> {
        Log.d(TAG, "Settling dice game: gameId=$gameId")
        return safeApiFlow { apiService.settleGame(gameId) }
    }

    fun getGameStatus(gameId: Long): Flow<ApiResult<DiceGameStatusResponse>> {
        Log.d(TAG, "Fetching dice game status: gameId=$gameId")
        return safeApiFlow { apiService.getGameStatus(gameId) }
    }

    fun getBalance(): Flow<ApiResult<DiceBalanceResponse>> {
        Log.d(TAG, "Fetching vault balance")
        return safeApiFlow { apiService.getBalance() }
    }

    companion object {
        private const val TAG = "DiceRepository"
    }
}
