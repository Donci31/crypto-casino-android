package hu.bme.aut.cryptocasino.data.model.auth

data class UserLogin(
    val usernameOrEmail: String,
    val password: String,
)
