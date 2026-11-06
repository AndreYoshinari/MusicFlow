package br.com.musicflow.di

import android.app.Application
import androidx.test.core.app.ApplicationProvider
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
import br.com.musicflow.player.MusicPlayback
import br.com.musicflow.player.PlaybackSession
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.koinApplication
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class MusicFlowKoinTest {
    @Test
    fun applicationModulesResolve() {
        val koin = koinApplication {
            androidContext(ApplicationProvider.getApplicationContext())
            modules(musicFlowModules)
        }.koin

        assertSame(koin.get<TrackRepository>(), koin.get<TrackRepository>())
        assertSame(koin.get<TrackPagingRepository>(), koin.get<TrackPagingRepository>())
        assertSame(koin.get<ArtistRepository>(), koin.get<ArtistRepository>())
        assertSame(koin.get<FavoriteRepository>(), koin.get<FavoriteRepository>())
        assertSame(koin.get<PlaylistRepository>(), koin.get<PlaylistRepository>())
        assertSame(koin.get<PlaybackHistoryRepository>(), koin.get<PlaybackHistoryRepository>())
        assertSame(koin.get<PlaybackSession>(), koin.get<PlaybackSession>())
        assertSame(koin.get<MusicPlayback>(), koin.get<MusicPlayback>())
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
