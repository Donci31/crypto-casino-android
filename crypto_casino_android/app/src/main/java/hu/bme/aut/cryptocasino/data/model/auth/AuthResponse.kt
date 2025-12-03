package hu.bme.aut.cryptocasino.data.model.auth

import hu.bme.aut.cryptocasino.data.model.user.User

data class AuthResponse(
    val token: String,
    val refreshToken: String,
    val tokenType: String,
    val user: User,
)
