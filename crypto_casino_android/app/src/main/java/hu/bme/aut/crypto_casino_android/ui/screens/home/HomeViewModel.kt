package hu.bme.aut.crypto_casino_android.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bme.aut.crypto_casino_android.data.model.user.User
import hu.bme.aut.crypto_casino_android.data.repository.UserRepository
import hu.bme.aut.crypto_casino_android.data.util.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _userState = MutableStateFlow<ApiResult<User>?>(null)
    val userState: StateFlow<ApiResult<User>?> = _userState

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

    fun refreshAll() {
        getCurrentUser()
    }
}
