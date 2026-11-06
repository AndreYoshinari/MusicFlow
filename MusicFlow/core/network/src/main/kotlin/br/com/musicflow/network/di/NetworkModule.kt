package br.com.musicflow.network.di

import br.com.musicflow.network.BuildConfig
import br.com.musicflow.network.JamendoClient
import br.com.musicflow.network.JamendoConfig
import br.com.musicflow.network.api.JamendoApiService
import br.com.musicflow.network.createJamendoOkHttp
import br.com.musicflow.network.createJamendoRetrofit
import br.com.musicflow.network.jamendoJson
import org.koin.dsl.module
import retrofit2.Retrofit

// Singles stay lazy. startKoin does not build OkHttp or Retrofit on the main thread.
val networkModule = module {
    single { JamendoConfig(clientId = BuildConfig.JAMENDO_CLIENT_ID) }
    single { jamendoJson() }
    single { createJamendoOkHttp(get()) }
    single { createJamendoRetrofit(get(), get(), get()) }
    single<JamendoApiService> { get<Retrofit>().create(JamendoApiService::class.java) }
    single { JamendoClient(get(), get()) }
}
