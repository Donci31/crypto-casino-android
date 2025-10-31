package hu.bme.aut.crypto_casino_android.data.api

import hu.bme.aut.crypto_casino_android.data.model.dice.DiceBalanceResponse
import hu.bme.aut.crypto_casino_android.data.model.dice.DiceConfigResponse
import hu.bme.aut.crypto_casino_android.data.model.dice.DiceGameCreatedResponse
import hu.bme.aut.crypto_casino_android.data.model.dice.DiceGameRequest
import hu.bme.aut.crypto_casino_android.data.model.dice.DiceGameSettledResponse
import hu.bme.aut.crypto_casino_android.data.model.dice.DiceGameStatusResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface DiceApiService {

    @GET("games/dice/config")
    suspend fun getDiceConfig(): Response<DiceConfigResponse>

    @POST("games/dice/create")
    suspend fun createGame(@Body request: DiceGameRequest): Response<DiceGameCreatedResponse>

    @POST("games/dice/settle/{gameId}")
    suspend fun settleGame(@Path("gameId") gameId: Long): Response<DiceGameSettledResponse>

    @GET("games/dice/status/{gameId}")
    suspend fun getGameStatus(@Path("gameId") gameId: Long): Response<DiceGameStatusResponse>

    @GET("games/dice/balance")
    suspend fun getBalance(): Response<DiceBalanceResponse>
}
