package hu.bme.aut.crypto_casino_android.data.interceptor

import android.util.Log
import dagger.Lazy
import hu.bme.aut.crypto_casino_android.data.local.TokenManager
import hu.bme.aut.crypto_casino_android.data.repository.AuthRepository
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenRefreshInterceptor @Inject constructor(
    private val tokenManager: TokenManager,
    private val authRepository: Lazy<AuthRepository>
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val response = chain.proceed(originalRequest)

        val isAuthEndpoint = originalRequest.url.encodedPath.let {
            it.contains("/auth/login") || it.contains("/auth/register") || it.contains("/auth/refresh")
        }

        if ((response.code == 401 || response.code == 403) && !isAuthEndpoint) {
            Log.d(TAG, "${response.code} received for ${originalRequest.url.encodedPath}, attempting token refresh")
            response.close()

            val refreshResult = runBlocking {
                authRepository.get().refreshToken()
            }

            return if (refreshResult.isSuccess) {
                Log.d(TAG, "Token refresh successful, retrying request")
                val newToken = runBlocking { tokenManager.getAccessTokenValue() }
                val newRequest = originalRequest.newBuilder()
                    .header("Authorization", "Bearer $newToken")
                    .build()
                chain.proceed(newRequest)
            } else {
                Log.w(TAG, "Token refresh failed, logging out user")
                runBlocking { authRepository.get().logout() }
                chain.proceed(originalRequest)
            }
        }

        return response
    }

    companion object {
        private const val TAG = "TokenRefreshInterceptor"
    }
}
