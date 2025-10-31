package hu.bme.aut.crypto_casino_android.data.api

import hu.bme.aut.crypto_casino_android.data.model.auth.AuthResponse
import hu.bme.aut.crypto_casino_android.data.model.auth.RefreshTokenRequest
import hu.bme.aut.crypto_casino_android.data.model.auth.UserLogin
import hu.bme.aut.crypto_casino_android.data.model.auth.UserRegistration
import hu.bme.aut.crypto_casino_android.data.model.user.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/register")
    suspend fun register(@Body userRegistration: UserRegistration): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body userLogin: UserLogin): Response<AuthResponse>

    @POST("auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<AuthResponse>
}
