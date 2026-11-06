package br.com.musicflow.network

data class JamendoConfig(
    val clientId: String,
    val baseUrl: String = DEFAULT_BASE_URL,
    val connectTimeoutMillis: Long = DEFAULT_CONNECT_TIMEOUT_MILLIS,
    val readTimeoutMillis: Long = DEFAULT_READ_TIMEOUT_MILLIS,
    val writeTimeoutMillis: Long = DEFAULT_WRITE_TIMEOUT_MILLIS,
) {
    init {
        require(baseUrl.endsWith("/")) { "Jamendo base URL must end with a slash." }
        require(connectTimeoutMillis > 0) { "Connect timeout must be positive." }
        require(readTimeoutMillis > 0) { "Read timeout must be positive." }
        require(writeTimeoutMillis > 0) { "Write timeout must be positive." }
    }

    private companion object {
        const val DEFAULT_BASE_URL = "https://api.jamendo.com/v3.0/"
        const val DEFAULT_CONNECT_TIMEOUT_MILLIS = 10_000L
        const val DEFAULT_READ_TIMEOUT_MILLIS = 20_000L
        const val DEFAULT_WRITE_TIMEOUT_MILLIS = 20_000L
    }
}
