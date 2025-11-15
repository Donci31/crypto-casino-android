package hu.bme.aut.crypto_casino_android.data.repository

import hu.bme.aut.crypto_casino_android.data.api.StatsApi
import hu.bme.aut.crypto_casino_android.data.model.stats.QuickStatsResponse
import hu.bme.aut.crypto_casino_android.data.model.stats.UserStatsResponse
import hu.bme.aut.crypto_casino_android.data.util.ApiResult
import hu.bme.aut.crypto_casino_android.data.util.safeApiFlow
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatsRepository @Inject constructor(
    private val statsApi: StatsApi
) {

    fun getUserStats(): Flow<ApiResult<UserStatsResponse>> {
        return safeApiFlow { statsApi.getUserStats() }
    }

    fun getQuickStats(): Flow<ApiResult<QuickStatsResponse>> {
        return safeApiFlow { statsApi.getQuickStats() }
    }
}
