package hu.bme.aut.crypto_casino_android.data.api


import hu.bme.aut.crypto_casino_android.data.model.slot.BalanceResponse
import hu.bme.aut.crypto_casino_android.data.model.slot.GameHistoryResponse
import hu.bme.aut.crypto_casino_android.data.model.slot.SlotConfigResponse
import hu.bme.aut.crypto_casino_android.data.model.slot.SpinRequest
import hu.bme.aut.crypto_casino_android.data.model.slot.SpinResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface SlotMachineApiService {

    @GET("games/slots/config")
    suspend fun getSlotConfig(): Response<SlotConfigResponse>

    @POST("games/slots/spin")
    suspend fun spin(@Body request: SpinRequest): Response<SpinResponse>

    @GET("games/slots/history")
    suspend fun getGameHistory(): Response<List<GameHistoryResponse>>

    @GET("games/slots/balance")
    suspend fun getBalance(): Response<BalanceResponse>
}
