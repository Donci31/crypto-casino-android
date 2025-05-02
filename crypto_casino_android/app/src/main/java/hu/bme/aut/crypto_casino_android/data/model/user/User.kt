package hu.bme.aut.crypto_casino_android.data.model.user

import java.time.LocalDateTime

data class User(
    val id: Long? = null,
    val username: String,
    val email: String,
    val password: String? = null,
    val ethereumAddress: String? = null,
    val createdAt: LocalDateTime? = null,
    val lastLogin: LocalDateTime? = null,
)
