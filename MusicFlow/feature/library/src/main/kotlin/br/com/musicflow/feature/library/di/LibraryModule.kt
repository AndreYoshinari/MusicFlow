package br.com.musicflow.feature.library.di

import br.com.musicflow.feature.library.FavoritesViewModel
import br.com.musicflow.feature.library.HistoryViewModel
import br.com.musicflow.feature.library.PlaylistsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val libraryModule = module {
    viewModel { FavoritesViewModel(get(), get()) }
    viewModel { PlaylistsViewModel(get(), get()) }
    viewModel { HistoryViewModel(get()) }
}
