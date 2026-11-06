package br.com.musicflow.network

sealed interface NetworkResult<out T> {
    data class Success<T>(val value: T) : NetworkResult<T>

    data class Failure(val error: NetworkError) : NetworkResult<Nothing>
}

sealed interface NetworkError {
    data object Offline : NetworkError

    data object Timeout : NetworkError

    data object InvalidJson : NetworkError

    data object MissingClientId : NetworkError

    data object Unavailable : NetworkError

    data class Http(val code: Int) : NetworkError

    data class Api(val code: Int, val message: String) : NetworkError
}
