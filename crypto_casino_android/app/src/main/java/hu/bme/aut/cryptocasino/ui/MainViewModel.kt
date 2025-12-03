package hu.bme.aut.cryptocasino.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bme.aut.cryptocasino.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel
    @Inject
    constructor(
        private val authRepository: AuthRepository,
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
                _authState.value =
                    if (!token.isNullOrEmpty()) {
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
            data object Loading : AuthState()

            data object Authenticated : AuthState()

            data object Unauthenticated : AuthState()
        }
    }
