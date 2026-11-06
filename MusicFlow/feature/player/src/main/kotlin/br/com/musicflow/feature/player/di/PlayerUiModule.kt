package br.com.musicflow.feature.player.di

import br.com.musicflow.feature.player.NowPlayingFavoriteViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val playerUiModule = module {
    viewModel { NowPlayingFavoriteViewModel(get(), get()) }
}
