package hu.bme.aut.crypto_casino_android.data.repository

import android.util.Log
import hu.bme.aut.crypto_casino_android.data.api.UserApi
import hu.bme.aut.crypto_casino_android.data.model.user.User
import hu.bme.aut.crypto_casino_android.data.util.ApiResult
import hu.bme.aut.crypto_casino_android.data.util.safeApiFlow
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val userApi: UserApi
) {
    fun getCurrentUser(): Flow<ApiResult<User>> {
        Log.d(TAG, "Fetching current user")
        return safeApiFlow { userApi.getCurrentUser() }
    }

    companion object {
        private const val TAG = "UserRepository"
    }
}
