package hu.bme.aut.cryptocasino.data.api

import hu.bme.aut.cryptocasino.data.model.user.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT

interface UserApi {
    @GET("users")
    suspend fun getCurrentUser(): Response<User>

    @PUT("users")
    suspend fun updateUser(
        @Body user: User,
    ): Response<User>
}
