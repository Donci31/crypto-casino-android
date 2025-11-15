package hu.bme.aut.crypto_casino_android.data.api

import hu.bme.aut.crypto_casino_android.data.model.roulette.RouletteBalanceResponse
import hu.bme.aut.crypto_casino_android.data.model.roulette.RouletteConfigResponse
import hu.bme.aut.crypto_casino_android.data.model.roulette.RouletteGameCreatedResponse
import hu.bme.aut.crypto_casino_android.data.model.roulette.RouletteGameRequest
import hu.bme.aut.crypto_casino_android.data.model.roulette.RouletteGameSettledResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface RouletteApiService {

    @GET("games/roulette/config")
    suspend fun getRouletteConfig(): Response<RouletteConfigResponse>

    @POST("games/roulette/create")
    suspend fun createGame(@Body request: RouletteGameRequest): Response<RouletteGameCreatedResponse>

    @POST("games/roulette/settle/{gameId}")
    suspend fun settleGame(@Path("gameId") gameId: Long): Response<RouletteGameSettledResponse>

    @GET("games/roulette/balance")
    suspend fun getBalance(): Response<RouletteBalanceResponse>
}
