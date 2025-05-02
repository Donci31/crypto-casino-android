package hu.bme.aut.crypto_casino_android.data.api

import hu.bme.aut.crypto_casino_android.data.model.user.User
import retrofit2.Response
import retrofit2.http.*

interface UserApi {
    @GET("users")
    suspend fun getCurrentUser(): Response<User>

    @PUT("users")
    suspend fun updateUser(@Body user: User): Response<User>
}
