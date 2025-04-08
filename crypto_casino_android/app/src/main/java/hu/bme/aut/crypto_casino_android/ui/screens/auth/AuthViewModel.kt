package hu.bme.aut.crypto_casino_android.ui.screens.auth

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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<ApiResult<AuthResponse>?>(null)
    val loginState: StateFlow<ApiResult<AuthResponse>?> = _loginState

    private val _registerState = MutableStateFlow<ApiResult<Boolean>?>(null)
    val registerState: StateFlow<ApiResult<Boolean>?> = _registerState

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
                .catch { error ->
                    _registerState.value = ApiResult.Error(error)
                }
                .collect { result ->
                    when (result) {
                        is ApiResult.Success -> _registerState.value = ApiResult.Success(true)
                        is ApiResult.Error -> _registerState.value = result
                        ApiResult.Loading -> _registerState.value = ApiResult.Loading
                    }
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _loginState.value = null
            _registerState.value = null
        }
    }

    fun resetStates() {
        _loginState.value = null
        _registerState.value = null
    }
}
