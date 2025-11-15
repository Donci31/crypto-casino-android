package hu.bme.aut.crypto_casino_android.ui.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bme.aut.crypto_casino_android.data.model.auth.AuthResponse
import hu.bme.aut.crypto_casino_android.data.model.auth.UserLogin
import hu.bme.aut.crypto_casino_android.data.model.auth.UserRegistration
import hu.bme.aut.crypto_casino_android.data.repository.AuthRepository
import hu.bme.aut.crypto_casino_android.data.util.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<ApiResult<AuthResponse>?>(null)
    val loginState: StateFlow<ApiResult<AuthResponse>?> = _loginState

    private val _registerState = MutableStateFlow<ApiResult<AuthResponse>?>(null)
    val registerState: StateFlow<ApiResult<AuthResponse>?> = _registerState

    fun login(username: String, password: String) {
        viewModelScope.launch {
            val userLogin = UserLogin(usernameOrEmail = username, password = password)
            authRepository.login(userLogin)
                .collect { result ->
                    _loginState.value = result
                }
        }
    }

    fun register(username: String, email: String, password: String) {
        viewModelScope.launch {
            val userRegistration = UserRegistration(
                username = username,
                email = email,
                password = password
            )
            authRepository.register(userRegistration)
                .collect { result ->
                    _registerState.value = result
                }
        }
    }

    fun resetStates() {
        _loginState.value = null
        _registerState.value = null
    }
}
