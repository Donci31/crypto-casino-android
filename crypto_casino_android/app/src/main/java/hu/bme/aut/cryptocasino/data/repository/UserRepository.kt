package hu.bme.aut.cryptocasino.data.repository

import android.util.Log
import hu.bme.aut.cryptocasino.data.api.UserApi
import hu.bme.aut.cryptocasino.data.model.user.User
import hu.bme.aut.cryptocasino.data.util.ApiResult
import hu.bme.aut.cryptocasino.data.util.safeApiFlow
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UserRepository
    @Inject
    constructor(
        private val userApi: UserApi,
    ) {
        fun getCurrentUser(): Flow<ApiResult<User>> {
            Log.d(TAG, "Fetching current user")
            return safeApiFlow { userApi.getCurrentUser() }
        }

        companion object {
            private const val TAG = "UserRepository"
        }
    }
