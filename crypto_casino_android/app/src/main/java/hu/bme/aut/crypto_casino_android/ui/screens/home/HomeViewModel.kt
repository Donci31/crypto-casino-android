package hu.bme.aut.crypto_casino_android.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hu.bme.aut.crypto_casino_android.data.model.blockchain.NetworkInfo
import hu.bme.aut.crypto_casino_android.data.model.blockchain.TokenRate
import hu.bme.aut.crypto_casino_android.data.model.user.User
import hu.bme.aut.crypto_casino_android.data.repository.BlockchainRepository
import hu.bme.aut.crypto_casino_android.data.repository.UserRepository
import hu.bme.aut.crypto_casino_android.data.util.ApiResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val blockchainRepository: BlockchainRepository
) : ViewModel() {

    private val _userState = MutableStateFlow<ApiResult<User>?>(null)
    val userState: StateFlow<ApiResult<User>?> = _userState

    private val _networkInfoState = MutableStateFlow<ApiResult<NetworkInfo>?>(null)
    val networkInfoState: StateFlow<ApiResult<NetworkInfo>?> = _networkInfoState

    private val _tokenRateState = MutableStateFlow<ApiResult<TokenRate>?>(null)
    val tokenRateState: StateFlow<ApiResult<TokenRate>?> = _tokenRateState

    init {
        getCurrentUser()
        getNetworkInfo()
        getTokenRate()
    }

    fun getCurrentUser() {
        viewModelScope.launch {
            userRepository.getCurrentUser()
                .collect { result ->
                    _userState.value = result
                }
        }
    }

    fun getNetworkInfo() {
        viewModelScope.launch {
            blockchainRepository.getNetworkInfo()
                .collect { result ->
                    _networkInfoState.value = result
                }
        }
    }

    fun getTokenRate() {
        viewModelScope.launch {
            blockchainRepository.getTokenRate()
                .collect { result ->
                    _tokenRateState.value = result
                }
        }
    }

    fun refreshAll() {
        getCurrentUser()
        getNetworkInfo()
        getTokenRate()
    }
}
