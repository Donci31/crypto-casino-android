package hu.bme.aut.cryptocasino.data.api

import hu.bme.aut.cryptocasino.data.model.stats.QuickStatsResponse
import hu.bme.aut.cryptocasino.data.model.stats.UserStatsResponse
import retrofit2.Response
import retrofit2.http.GET

interface StatsApi {
    @GET("stats")
    suspend fun getUserStats(): Response<UserStatsResponse>

    @GET("stats/quick")
    suspend fun getQuickStats(): Response<QuickStatsResponse>
}
