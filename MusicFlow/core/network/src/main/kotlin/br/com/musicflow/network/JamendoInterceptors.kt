package br.com.musicflow.network

import okhttp3.Interceptor
import okhttp3.Response

internal class JamendoClientIdInterceptor(
    private val clientId: String,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val url = chain.request().url.newBuilder()
            .addQueryParameter("client_id", clientId)
            .build()
        val request = chain.request().newBuilder().url(url).build()
        return chain.proceed(request)
    }
}

internal class AcceptJsonInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .header("Accept", "application/json")
            .build()
        return chain.proceed(request)
    }
}
