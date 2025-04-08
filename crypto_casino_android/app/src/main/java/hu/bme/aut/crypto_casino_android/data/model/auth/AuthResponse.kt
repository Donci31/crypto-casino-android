package hu.bme.aut.crypto_casino_android.data.model.auth

import hu.bme.aut.crypto_casino_android.data.model.user.User

data class AuthResponse(
    val token: String,
    val tokenType: String,
    val user: User
)
