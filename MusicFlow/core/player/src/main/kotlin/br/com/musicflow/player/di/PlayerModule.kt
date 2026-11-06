package br.com.musicflow.player.di

import br.com.musicflow.player.AudioEngine
import br.com.musicflow.player.Media3AudioEngine
import br.com.musicflow.player.MusicPlayback
import br.com.musicflow.player.PlaybackController
import br.com.musicflow.player.PlaybackSession
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val playerModule = module {
    single { PlaybackSession() }
    single { Media3AudioEngine(androidContext()) }
    single<AudioEngine> { get<Media3AudioEngine>() }
    single<MusicPlayback> { PlaybackController(get()) }
}
