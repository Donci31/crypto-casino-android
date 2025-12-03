package hu.bme.aut.cryptocasino.data.repository

import hu.bme.aut.cryptocasino.data.api.StatsApi
import hu.bme.aut.cryptocasino.data.model.stats.QuickStatsResponse
import hu.bme.aut.cryptocasino.data.model.stats.UserStatsResponse
import hu.bme.aut.cryptocasino.data.util.ApiResult
import hu.bme.aut.cryptocasino.data.util.safeApiFlow
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatsRepository
    @Inject
    constructor(
        private val statsApi: StatsApi,
    ) {
        fun getUserStats(): Flow<ApiResult<UserStatsResponse>> = safeApiFlow { statsApi.getUserStats() }

        fun getQuickStats(): Flow<ApiResult<QuickStatsResponse>> = safeApiFlow { statsApi.getQuickStats() }
    }
