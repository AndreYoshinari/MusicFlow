package br.com.musicflow.feature.playlist.di

import br.com.musicflow.feature.playlist.PlaylistDetailsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val playlistModule = module {
    viewModel { (playlistId: Long) ->
        PlaylistDetailsViewModel(
            playlistId = playlistId,
            playlist = get(),
            renamePlaylist = get(),
            deletePlaylist = get(),
            addTrack = get(),
            removeTrack = get(),
            favorites = get(),
            details = get(),
            history = get(),
        )
    }
}
