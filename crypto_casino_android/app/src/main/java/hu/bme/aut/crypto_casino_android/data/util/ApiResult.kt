package hu.bme.aut.crypto_casino_android.data.util

sealed class ApiResult<out T> {
    data class Success<out T>(val data: T) : ApiResult<T>()
    data class Error(val exception: Throwable) : ApiResult<Nothing>()
    data object Loading : ApiResult<Nothing>()
}
