package hu.bme.aut.crypto_casino_android.data.repository

import hu.bme.aut.crypto_casino_android.data.api.UserApi
import hu.bme.aut.crypto_casino_android.data.model.user.User
import hu.bme.aut.crypto_casino_android.data.util.ApiResult
import hu.bme.aut.crypto_casino_android.data.util.safeApiFlow
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val userApi: UserApi
) {
    fun getCurrentUser(): Flow<ApiResult<User>> =
        safeApiFlow { userApi.getCurrentUser() }

    fun updateUser(user: User): Flow<ApiResult<User>> =
        safeApiFlow { userApi.updateUser(user) }
}
