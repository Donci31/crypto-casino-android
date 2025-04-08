package hu.bme.aut.crypto_casino_android.data.repository

import hu.bme.aut.crypto_casino_android.data.api.AuthApi
import hu.bme.aut.crypto_casino_android.data.local.TokenManager
import hu.bme.aut.crypto_casino_android.data.model.auth.AuthResponse
import hu.bme.aut.crypto_casino_android.data.model.auth.UserLogin
import hu.bme.aut.crypto_casino_android.data.model.auth.UserRegistration
import hu.bme.aut.crypto_casino_android.data.model.user.User
import hu.bme.aut.crypto_casino_android.data.util.ApiResult
import hu.bme.aut.crypto_casino_android.data.util.safeApiFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import java.net.SocketTimeoutException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val tokenManager: TokenManager
) {
    fun register(userRegistration: UserRegistration): Flow<ApiResult<User>> =
        safeApiFlow { authApi.register(userRegistration) }

    fun login(userLogin: UserLogin): Flow<ApiResult<AuthResponse>> = flow {
        val response = authApi.login(userLogin)
        if (response.isSuccessful) {
            response.body()?.let {
                tokenManager.saveToken(it.token)
                emit(ApiResult.Success(it))
            } ?: emit(ApiResult.Error(Exception("Empty response body")))
        } else {
            emit(ApiResult.Error(Exception("Login failed: ${response.code()}")))
        }
    }.onStart {
        emit(ApiResult.Loading)
    }.catch { e ->
        if (e is SocketTimeoutException) {
            emit(ApiResult.Error(Exception("Connection timed out")))
        } else {
            emit(ApiResult.Error(e))
        }
    }

    suspend fun logout() {
        tokenManager.deleteToken()
    }

    fun getAuthToken(): Flow<String?> = tokenManager.getToken
}
