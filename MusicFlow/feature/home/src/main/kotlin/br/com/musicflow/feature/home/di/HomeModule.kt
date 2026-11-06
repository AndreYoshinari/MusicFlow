package br.com.musicflow.feature.home.di

import br.com.musicflow.feature.home.HomeViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val homeModule = module {
    viewModel {
        HomeViewModel(
            observePopularTracks = get(),
            featuredArtists = get(),
            popularAlbums = get(),
            recentTracks = get(),
            playlists = get(),
        )
    }
}
