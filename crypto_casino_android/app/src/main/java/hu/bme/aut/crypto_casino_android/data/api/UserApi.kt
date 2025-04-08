package hu.bme.aut.crypto_casino_android.data.api

import hu.bme.aut.crypto_casino_android.data.model.user.User
import retrofit2.Response
import retrofit2.http.*

interface UserApi {
    @GET("users/me")
    suspend fun getCurrentUser(): Response<User>

    @GET("users/{id}")
    suspend fun getUserById(@Path("id") id: Long): Response<User>

    @PUT("users/{id}")
    suspend fun updateUser(@Path("id") id: Long, @Body user: User): Response<User>
}
