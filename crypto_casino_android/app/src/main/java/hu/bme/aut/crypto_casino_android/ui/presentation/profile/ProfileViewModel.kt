package hu.bme.aut.crypto_casino_android.ui.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bme.aut.crypto_casino_android.data.model.stats.UserStatsResponse
import hu.bme.aut.crypto_casino_android.data.model.user.User
import hu.bme.aut.crypto_casino_android.data.repository.AuthRepository
import hu.bme.aut.crypto_casino_android.data.repository.StatsRepository
import hu.bme.aut.crypto_casino_android.data.repository.UserRepository
import hu.bme.aut.crypto_casino_android.data.util.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
    private val statsRepository: StatsRepository
) : ViewModel() {

    private val _userState = MutableStateFlow<ApiResult<User>?>(null)
    val userState: StateFlow<ApiResult<User>?> = _userState

    private val _updateState = MutableStateFlow<ApiResult<User>?>(null)
    val updateState: StateFlow<ApiResult<User>?> = _updateState

    private val _statsState = MutableStateFlow<ApiResult<UserStatsResponse>?>(null)
    val statsState: StateFlow<ApiResult<UserStatsResponse>?> = _statsState

    init {
        getCurrentUser()
        getUserStats()
    }

    fun getCurrentUser() {
        viewModelScope.launch {
            userRepository.getCurrentUser()
                .collect { result ->
                    _userState.value = result
                }
        }
    }

    fun getUserStats() {
        viewModelScope.launch {
            statsRepository.getUserStats()
                .collect { result ->
                    _statsState.value = result
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
