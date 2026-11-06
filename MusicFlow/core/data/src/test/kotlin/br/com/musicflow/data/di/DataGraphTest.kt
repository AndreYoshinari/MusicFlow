package br.com.musicflow.data.di

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import br.com.musicflow.common.di.domainModule
import br.com.musicflow.common.domain.repository.ArtistRepository
import br.com.musicflow.common.domain.repository.FavoriteRepository
import br.com.musicflow.common.domain.repository.PlaybackHistoryRepository
import br.com.musicflow.common.domain.repository.PlaylistRepository
import br.com.musicflow.common.domain.repository.TrackPagingRepository
import br.com.musicflow.common.domain.repository.TrackRepository
import br.com.musicflow.common.domain.usecase.GetPopularTracksUseCase
import br.com.musicflow.database.MusicFlowDatabase
import br.com.musicflow.database.dao.CachedAlbumDao
import br.com.musicflow.database.dao.CachedArtistDao
import br.com.musicflow.database.dao.CachedTrackDao
import br.com.musicflow.database.dao.CatalogCacheDao
import br.com.musicflow.database.dao.CatalogPageDao
import br.com.musicflow.database.dao.CatalogSnapshotDao
import br.com.musicflow.database.dao.FavoriteDao
import br.com.musicflow.database.dao.PlaybackHistoryDao
import br.com.musicflow.database.dao.PlaylistDao
import br.com.musicflow.network.JamendoClient
import br.com.musicflow.network.api.JamendoApiService
import okhttp3.OkHttpClient
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.core.Koin
import org.koin.dsl.koinApplication
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.reflect.KClass

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class DataGraphTest {
    @Test
    fun dataAndDomainShareTheInfrastructure() {
        val koin = koinApplication {
            androidContext(ApplicationProvider.getApplicationContext())
            modules(dataModule, domainModule)
        }.koin

        listOf(
            OkHttpClient::class,
            JamendoApiService::class,
            JamendoClient::class,
            MusicFlowDatabase::class,
            CachedTrackDao::class,
            CachedArtistDao::class,
            CachedAlbumDao::class,
            CatalogSnapshotDao::class,
            CatalogCacheDao::class,
            CatalogPageDao::class,
            FavoriteDao::class,
            PlaylistDao::class,
            PlaybackHistoryDao::class,
            TrackRepository::class,
            TrackPagingRepository::class,
            ArtistRepository::class,
            FavoriteRepository::class,
            PlaylistRepository::class,
            PlaybackHistoryRepository::class,
        ).forEach { type -> koin.assertSingle(type) }

        assertNotSame(koin.get<GetPopularTracksUseCase>(), koin.get<GetPopularTracksUseCase>())
    }
}

private fun Koin.assertSingle(type: KClass<*>) {
    assertSame(get(type), get(type))
}
