package hu.bme.aut.crypto_casino_android.data.repository

import android.util.Log
import hu.bme.aut.crypto_casino_android.data.api.AuthApi
import hu.bme.aut.crypto_casino_android.data.local.TokenManager
import hu.bme.aut.crypto_casino_android.data.model.auth.AuthResponse
import hu.bme.aut.crypto_casino_android.data.model.auth.RefreshTokenRequest
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
        Log.d(TAG, "Login attempt for: ${userLogin.usernameOrEmail}")
        val response = authApi.login(userLogin)
        if (response.isSuccessful) {
            response.body()?.let {
                Log.d(TAG, "Login successful, saving tokens")
                tokenManager.saveTokens(it.token, it.refreshToken)
                emit(ApiResult.Success(it))
            } ?: run {
                Log.e(TAG, "Login response body is empty")
                emit(ApiResult.Error(Exception("Empty response body")))
            }
        } else {
            Log.e(TAG, "Login failed: ${response.code()}")
            emit(ApiResult.Error(Exception("Login failed: ${response.code()}")))
        }
    }.onStart {
        emit(ApiResult.Loading)
    }.catch { e ->
        Log.e(TAG, "Login exception: ${e.message}", e)
        if (e is SocketTimeoutException) {
            emit(ApiResult.Error(Exception("Connection timed out")))
        } else {
            emit(ApiResult.Error(e))
        }
    }

    suspend fun refreshToken(): Result<AuthResponse> {
        return try {
            Log.d(TAG, "Attempting token refresh")
            val refreshToken = tokenManager.getRefreshTokenValue()
            if (refreshToken == null) {
                Log.w(TAG, "No refresh token available")
                return Result.failure(Exception("No refresh token available"))
            }

            val response = authApi.refreshToken(RefreshTokenRequest(refreshToken))
            if (response.isSuccessful) {
                response.body()?.let { authResponse ->
                    Log.d(TAG, "Token refresh successful, saving new tokens")
                    tokenManager.saveTokens(authResponse.token, authResponse.refreshToken)
                    Result.success(authResponse)
                } ?: run {
                    Log.e(TAG, "Refresh response body is empty")
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                Log.e(TAG, "Token refresh failed: ${response.code()}")
                Result.failure(Exception("Token refresh failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Token refresh exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun logout() {
        Log.d(TAG, "Logging out, deleting tokens")
        tokenManager.deleteTokens()
    }

    companion object {
        private const val TAG = "AuthRepository"
    }

    fun getAuthToken(): Flow<String?> = tokenManager.getAccessToken
}
