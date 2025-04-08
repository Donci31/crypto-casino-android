package hu.bme.aut.crypto_casino_android.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hu.bme.aut.crypto_casino_android.data.model.user.User
import hu.bme.aut.crypto_casino_android.data.repository.AuthRepository
import hu.bme.aut.crypto_casino_android.data.repository.UserRepository
import hu.bme.aut.crypto_casino_android.data.util.ApiResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _userState = MutableStateFlow<ApiResult<User>?>(null)
    val userState: StateFlow<ApiResult<User>?> = _userState

    private val _updateState = MutableStateFlow<ApiResult<User>?>(null)
    val updateState: StateFlow<ApiResult<User>?> = _updateState

    init {
        getCurrentUser()
    }

    fun getCurrentUser() {
        viewModelScope.launch {
            userRepository.getCurrentUser()
                .collect { result ->
                    _userState.value = result
                }
        }
    }

    fun updateUser(user: User) {
        viewModelScope.launch {
            user.id?.let { userId ->
                userRepository.updateUser(userId, user)
                    .collect { result ->
                        _updateState.value = result
                        if (result is ApiResult.Success) {
                            _userState.value = result
                        }
                    }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }

    fun resetUpdateState() {
        _updateState.value = null
    }
}
