package br.com.musicflow.feature.search.di

import br.com.musicflow.feature.search.SearchViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val searchModule = module {
    viewModel {
        SearchViewModel(
            observeTrackSearch = get(),
            searchArtists = get(),
        )
    }
}
