package br.com.musicflow.common.di

import br.com.musicflow.common.domain.repository.ArtistRepository
import br.com.musicflow.common.domain.repository.FavoriteRepository
import br.com.musicflow.common.domain.repository.PlaybackHistoryRepository
import br.com.musicflow.common.domain.repository.PlaylistRepository
import br.com.musicflow.common.domain.repository.TrackPagingRepository
import br.com.musicflow.common.domain.repository.TrackRepository
import br.com.musicflow.common.domain.usecase.AddTrackToHistoryUseCase
import br.com.musicflow.common.domain.usecase.AddTrackToPlaylistUseCase
import br.com.musicflow.common.domain.usecase.CreatePlaylistUseCase
import br.com.musicflow.common.domain.usecase.DeletePlaylistUseCase
import br.com.musicflow.common.domain.usecase.GetPlaylistUseCase
import br.com.musicflow.common.domain.usecase.RenamePlaylistUseCase
import br.com.musicflow.common.domain.usecase.GetFavoritesUseCase
import br.com.musicflow.common.domain.usecase.GetFeaturedArtistsUseCase
import br.com.musicflow.common.domain.usecase.GetPlaylistsUseCase
import br.com.musicflow.common.domain.usecase.GetPopularAlbumsUseCase
import br.com.musicflow.common.domain.usecase.GetPopularTracksUseCase
import br.com.musicflow.common.domain.usecase.GetRecentTracksUseCase
import br.com.musicflow.common.domain.usecase.GetRecentlyPlayedUseCase
import br.com.musicflow.common.domain.usecase.ObservePopularTracksUseCase
import br.com.musicflow.common.domain.usecase.GetTrackDetailsUseCase
import br.com.musicflow.common.domain.usecase.GetTrackStreamUrlUseCase
import br.com.musicflow.common.domain.usecase.GetTracksByArtistUseCase
import br.com.musicflow.common.domain.usecase.ObserveTrackSearchUseCase
import br.com.musicflow.common.domain.usecase.RemoveTrackFromPlaylistUseCase
import br.com.musicflow.common.domain.usecase.SearchArtistsUseCase
import br.com.musicflow.common.domain.usecase.SearchTracksUseCase
import br.com.musicflow.common.domain.usecase.ToggleFavoriteUseCase
import io.mockk.mockk
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Test
import org.koin.dsl.koinApplication
import org.koin.dsl.module

class DomainModuleTest {
    @Test
    fun useCasesAreCreatedFromTheSharedRepositories() {
        val tracks = mockk<TrackRepository>()
        val paging = mockk<TrackPagingRepository>()
        val artists = mockk<ArtistRepository>()
        val favorites = mockk<FavoriteRepository>()
        val playlists = mockk<PlaylistRepository>()
        val history = mockk<PlaybackHistoryRepository>()
        val koin = koinApplication {
            modules(
                module {
                    single { tracks }
                    single { paging }
                    single { artists }
                    single { favorites }
                    single { playlists }
                    single { history }
                },
                domainModule,
            )
        }.koin

        assertSame(tracks, koin.get<TrackRepository>())
        assertNotSame(koin.get<GetPopularTracksUseCase>(), koin.get<GetPopularTracksUseCase>())
        listOf(
            ObservePopularTracksUseCase::class,
            GetFeaturedArtistsUseCase::class,
            GetPopularAlbumsUseCase::class,
            GetRecentTracksUseCase::class,
            SearchTracksUseCase::class,
            SearchArtistsUseCase::class,
            GetTrackDetailsUseCase::class,
            GetTracksByArtistUseCase::class,
            GetTrackStreamUrlUseCase::class,
            ObserveTrackSearchUseCase::class,
            GetFavoritesUseCase::class,
            ToggleFavoriteUseCase::class,
            CreatePlaylistUseCase::class,
            GetPlaylistUseCase::class,
            RenamePlaylistUseCase::class,
            DeletePlaylistUseCase::class,
            AddTrackToPlaylistUseCase::class,
            RemoveTrackFromPlaylistUseCase::class,
            GetPlaylistsUseCase::class,
            GetRecentlyPlayedUseCase::class,
            AddTrackToHistoryUseCase::class,
        ).forEach { useCase -> koin.get(useCase) }
    }
}
