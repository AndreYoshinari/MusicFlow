package br.com.musicflow.ui

import br.com.musicflow.common.domain.DomainResult
import br.com.musicflow.common.domain.Track
import br.com.musicflow.common.domain.TrackId
import br.com.musicflow.common.domain.repository.FavoriteRepository
import br.com.musicflow.common.domain.usecase.AddTrackToHistoryUseCase
import br.com.musicflow.common.domain.usecase.AddTrackToPlaylistUseCase
import br.com.musicflow.common.domain.usecase.CreatePlaylistUseCase
import br.com.musicflow.common.domain.usecase.DeletePlaylistUseCase
import br.com.musicflow.common.domain.usecase.GetFavoritesUseCase
import br.com.musicflow.common.domain.usecase.GetPlaylistUseCase
import br.com.musicflow.common.domain.usecase.GetPlaylistsUseCase
import br.com.musicflow.common.domain.usecase.GetRecentTracksUseCase
import br.com.musicflow.common.domain.usecase.GetRecentlyPlayedUseCase
import br.com.musicflow.common.domain.usecase.GetTrackDetailsUseCase
import br.com.musicflow.common.domain.usecase.RemoveTrackFromPlaylistUseCase
import br.com.musicflow.common.domain.usecase.RenamePlaylistUseCase
import br.com.musicflow.common.domain.usecase.ToggleFavoriteUseCase
import br.com.musicflow.feature.library.FavoritesViewModel
import br.com.musicflow.feature.library.HistoryViewModel
import br.com.musicflow.feature.library.PlaylistsViewModel
import br.com.musicflow.feature.playlist.PlaylistDetailsViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal fun emptyFavoritesViewModel(): FavoritesViewModel {
    val favorites = IdleFavorites()
    return FavoritesViewModel(
        observeFavorites = GetFavoritesUseCase(favorites),
        toggleFavorite = ToggleFavoriteUseCase(favorites),
    )
}

internal fun emptyPlaylistsViewModel(): PlaylistsViewModel = PlaylistsViewModel(
    playlists = GetPlaylistsUseCase(EmptyPlaylists()),
    createPlaylist = CreatePlaylistUseCase(EmptyPlaylists()),
)

internal fun emptyHistoryViewModel(): HistoryViewModel = HistoryViewModel(
    recentTracks = GetRecentTracksUseCase(
        history = GetRecentlyPlayedUseCase(EmptyHistory()),
        details = GetTrackDetailsUseCase(EmptyTracks()),
    ),
)

internal fun emptyPlaylistDetailsViewModel(playlistId: Long): PlaylistDetailsViewModel {
    val playlists = EmptyPlaylists()
    return PlaylistDetailsViewModel(
        playlistId = playlistId,
        playlist = GetPlaylistUseCase(playlists),
        renamePlaylist = RenamePlaylistUseCase(playlists),
        deletePlaylist = DeletePlaylistUseCase(playlists),
        addTrack = AddTrackToPlaylistUseCase(playlists),
        removeTrack = RemoveTrackFromPlaylistUseCase(playlists),
        favorites = GetFavoritesUseCase(IdleFavorites()),
        details = GetTrackDetailsUseCase(EmptyTracks()),
        history = AddTrackToHistoryUseCase(EmptyHistory()),
    )
}

private class IdleFavorites : FavoriteRepository {
    override suspend fun getFavorites(): DomainResult<List<Track>> = DomainResult.Success(emptyList())

    override suspend fun isFavorite(trackId: TrackId): DomainResult<Boolean> = DomainResult.Success(false)

    override fun observeFavorites(): Flow<List<Track>> = flowOf(emptyList())

    override suspend fun addFavorite(trackId: TrackId): DomainResult<Unit> = DomainResult.Success(Unit)

    override suspend fun removeFavorite(trackId: TrackId): DomainResult<Unit> = DomainResult.Success(Unit)
}
