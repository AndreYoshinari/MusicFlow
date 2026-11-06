package br.com.musicflow.ui

import androidx.paging.PagingData
import br.com.musicflow.common.domain.Album
import br.com.musicflow.common.domain.Artist
import br.com.musicflow.common.domain.ArtistId
import br.com.musicflow.common.domain.DomainError
import br.com.musicflow.common.domain.DomainResult
import br.com.musicflow.common.domain.Playlist
import br.com.musicflow.common.domain.PlaylistId
import br.com.musicflow.common.domain.Track
import br.com.musicflow.common.domain.TrackId
import br.com.musicflow.common.domain.repository.ArtistRepository
import br.com.musicflow.common.domain.repository.PlaybackHistoryRepository
import br.com.musicflow.common.domain.repository.PlaylistRepository
import br.com.musicflow.common.domain.repository.TrackPagingRepository
import br.com.musicflow.common.domain.repository.TrackRepository
import br.com.musicflow.common.domain.usecase.GetFeaturedArtistsUseCase
import br.com.musicflow.common.domain.usecase.GetPlaylistsUseCase
import br.com.musicflow.common.domain.usecase.GetPopularAlbumsUseCase
import br.com.musicflow.common.domain.usecase.GetRecentTracksUseCase
import br.com.musicflow.common.domain.usecase.GetRecentlyPlayedUseCase
import br.com.musicflow.common.domain.usecase.GetTrackDetailsUseCase
import br.com.musicflow.common.domain.usecase.ObservePopularTracksUseCase
import br.com.musicflow.common.domain.usecase.ObserveTrackSearchUseCase
import br.com.musicflow.common.domain.usecase.SearchArtistsUseCase
import br.com.musicflow.feature.home.HomeViewModel
import br.com.musicflow.feature.search.SearchViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.time.Instant

internal fun emptyHomeViewModel(): HomeViewModel = HomeViewModel(
    observePopularTracks = ObservePopularTracksUseCase(EmptyPaging()),
    featuredArtists = GetFeaturedArtistsUseCase(EmptyArtists()),
    popularAlbums = GetPopularAlbumsUseCase(EmptyArtists()),
    recentTracks = GetRecentTracksUseCase(
        history = GetRecentlyPlayedUseCase(EmptyHistory()),
        details = GetTrackDetailsUseCase(EmptyTracks()),
    ),
    playlists = GetPlaylistsUseCase(EmptyPlaylists()),
)

internal fun emptySearchViewModel(): SearchViewModel = SearchViewModel(
    observeTrackSearch = ObserveTrackSearchUseCase(EmptyPaging()),
    searchArtists = SearchArtistsUseCase(EmptyArtists()),
)

private class EmptyPaging : TrackPagingRepository {
    override fun popularTracks(): Flow<PagingData<Track>> = flowOf(PagingData.empty())

    override fun searchTracks(query: String): Flow<PagingData<Track>> = flowOf(PagingData.empty())

    override fun tracksByArtist(artistId: ArtistId): Flow<PagingData<Track>> = flowOf(PagingData.empty())
}

private class EmptyArtists : ArtistRepository {
    override suspend fun getArtistById(id: ArtistId): DomainResult<Artist> = missing()

    override suspend fun searchArtists(query: String): DomainResult<List<Artist>> = DomainResult.Success(emptyList())

    override suspend fun getFeaturedArtists(): DomainResult<List<Artist>> = DomainResult.Success(emptyList())

    override suspend fun getAlbumsByArtist(artistId: ArtistId): DomainResult<List<Album>> =
        DomainResult.Success(emptyList())

    override suspend fun getPopularAlbums(): DomainResult<List<Album>> = DomainResult.Success(emptyList())
}

internal class EmptyTracks : TrackRepository {
    override suspend fun getPopularTracks(): DomainResult<List<Track>> = DomainResult.Success(emptyList())

    override suspend fun searchTracks(query: String): DomainResult<List<Track>> = DomainResult.Success(emptyList())

    override suspend fun getTrackById(id: TrackId): DomainResult<Track> = missing()

    override suspend fun getTracksByArtist(artistId: ArtistId): DomainResult<List<Track>> =
        DomainResult.Success(emptyList())

    override suspend fun getTrackStreamUrl(id: TrackId): DomainResult<String> = missing()
}

internal class EmptyHistory : PlaybackHistoryRepository {
    override suspend fun getRecentlyPlayed(limit: Int): DomainResult<List<TrackId>> =
        DomainResult.Success(emptyList())

    override suspend fun addToHistory(trackId: TrackId, playedAt: Instant): DomainResult<Unit> =
        DomainResult.Success(Unit)

    override suspend fun clearHistory(): DomainResult<Unit> = DomainResult.Success(Unit)
}

internal class EmptyPlaylists : PlaylistRepository {
    override suspend fun getPlaylists(): DomainResult<List<Playlist>> = DomainResult.Success(emptyList())

    override suspend fun getPlaylistById(id: PlaylistId): DomainResult<Playlist> =
        DomainResult.Success(Playlist(id, "Playlist ${id.value}"))

    override suspend fun createPlaylist(name: String): DomainResult<Playlist> =
        DomainResult.Failure(DomainError.InvalidName)

    override suspend fun renamePlaylist(id: PlaylistId, name: String): DomainResult<Playlist> = missing()

    override suspend fun deletePlaylist(id: PlaylistId): DomainResult<Unit> = missing()

    override suspend fun addTrack(playlistId: PlaylistId, trackId: TrackId): DomainResult<Playlist> = missing()

    override suspend fun removeTrack(playlistId: PlaylistId, trackId: TrackId): DomainResult<Playlist> = missing()
}

private fun <T> missing(): DomainResult<T> = DomainResult.Failure(DomainError.NotFound)
