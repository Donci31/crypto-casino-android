package hu.bme.aut.cryptocasino.data.api

import hu.bme.aut.cryptocasino.data.model.auth.AuthResponse
import hu.bme.aut.cryptocasino.data.model.auth.RefreshTokenRequest
import hu.bme.aut.cryptocasino.data.model.auth.UserLogin
import hu.bme.aut.cryptocasino.data.model.auth.UserRegistration
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/register")
    suspend fun register(
        @Body userRegistration: UserRegistration,
    ): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(
        @Body userLogin: UserLogin,
    ): Response<AuthResponse>

    @POST("auth/refresh")
    suspend fun refreshToken(
        @Body request: RefreshTokenRequest,
    ): Response<AuthResponse>
}
