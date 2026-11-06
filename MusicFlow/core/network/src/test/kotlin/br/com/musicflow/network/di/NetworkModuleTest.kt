package br.com.musicflow.network.di

import br.com.musicflow.network.JamendoClient
import br.com.musicflow.network.api.JamendoApiService
import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import org.koin.dsl.koinApplication
import retrofit2.Retrofit

class NetworkModuleTest {
    @Test
    fun httpClientRetrofitAndServiceAreShared() {
        val koin = koinApplication { modules(networkModule) }.koin

        val client = koin.get<OkHttpClient>()
        val retrofit = koin.get<Retrofit>()
        assertSame(client, koin.get<OkHttpClient>())
        assertSame(retrofit, koin.get<Retrofit>())
        assertSame(client, retrofit.callFactory())
        assertEquals("https://api.jamendo.com/v3.0/", retrofit.baseUrl().toString())
        assertSame(koin.get<JamendoApiService>(), koin.get<JamendoApiService>())
        assertSame(koin.get<JamendoClient>(), koin.get<JamendoClient>())
    }
}
