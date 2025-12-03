package hu.bme.aut.cryptocasino.data.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import retrofit2.Response

inline fun <T> safeApiFlow(crossinline apiCall: suspend () -> Response<T>): Flow<ApiResult<T>> =
    flow {
        val response = apiCall()
        if (response.isSuccessful) {
            response.body()?.let {
                emit(ApiResult.Success(it))
            } ?: emit(ApiResult.Error(Exception("Empty response body")))
        } else {
            emit(ApiResult.Error(Exception("API call failed: ${response.code()}")))
        }
    }.onStart {
        emit(ApiResult.Loading)
    }
