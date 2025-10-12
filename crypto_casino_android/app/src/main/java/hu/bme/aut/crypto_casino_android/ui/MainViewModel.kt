package hu.bme.aut.crypto_casino_android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hu.bme.aut.crypto_casino_android.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState

    init {
        checkAuthState()
        observeAuthToken()
    }

    private fun checkAuthState() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val token = authRepository.getAuthToken().first()
            _authState.value = if (!token.isNullOrEmpty()) {
                AuthState.Authenticated
            } else {
                AuthState.Unauthenticated
            }
        }
    }

    private fun observeAuthToken() {
        viewModelScope.launch {
            authRepository.getAuthToken().collect { token ->
                // Only update to Unauthenticated if we were previously authenticated and token is now null
                if (token.isNullOrEmpty() && _authState.value == AuthState.Authenticated) {
                    _authState.value = AuthState.Unauthenticated
                }
            }
        }
    }

    sealed class AuthState {
        object Loading : AuthState()
        object Authenticated : AuthState()
        object Unauthenticated : AuthState()
    }
}
