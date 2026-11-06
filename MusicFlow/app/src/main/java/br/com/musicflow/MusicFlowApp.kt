package br.com.musicflow

import android.app.Application
import br.com.musicflow.di.musicFlowModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MusicFlowApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MusicFlowApp)
            modules(musicFlowModules)
        }
    }
}
