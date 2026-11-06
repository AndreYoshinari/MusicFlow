package br.com.musicflow.data.di

import br.com.musicflow.common.domain.repository.ArtistRepository
import br.com.musicflow.common.domain.repository.FavoriteRepository
import br.com.musicflow.common.domain.repository.PlaybackHistoryRepository
import br.com.musicflow.common.domain.repository.PlaylistRepository
import br.com.musicflow.common.domain.repository.TrackPagingRepository
import br.com.musicflow.common.domain.repository.TrackRepository
import br.com.musicflow.data.local.ArtistStore
import br.com.musicflow.data.local.FavoriteLocalDataSource
import br.com.musicflow.data.local.PlaybackHistoryLocalDataSource
import br.com.musicflow.data.local.PlaylistLocalDataSource
import br.com.musicflow.data.local.RoomArtistStore
import br.com.musicflow.data.local.RoomFavoriteLocalDataSource
import br.com.musicflow.data.local.RoomPlaybackHistoryLocalDataSource
import br.com.musicflow.data.local.RoomPlaylistLocalDataSource
import br.com.musicflow.data.local.RoomTrackStore
import br.com.musicflow.data.local.TrackStore
import br.com.musicflow.data.paging.RoomTrackPageStore
import br.com.musicflow.data.paging.TrackPageStore
import br.com.musicflow.data.remote.JamendoArtistDataSource
import br.com.musicflow.data.remote.JamendoTrackDataSource
import br.com.musicflow.data.repository.ArtistRepositoryImpl
import br.com.musicflow.data.repository.FavoriteRepositoryImpl
import br.com.musicflow.data.repository.PlaybackHistoryRepositoryImpl
import br.com.musicflow.data.repository.PlaylistRepositoryImpl
import br.com.musicflow.data.repository.RequestGate
import br.com.musicflow.data.repository.TrackPagingRepositoryImpl
import br.com.musicflow.data.repository.TrackRepositoryImpl
import br.com.musicflow.database.di.databaseModule
import br.com.musicflow.network.di.networkModule
import org.koin.dsl.module

val dataModule = module {
    includes(networkModule, databaseModule)
    single { JamendoTrackDataSource(get()) }
    single { JamendoArtistDataSource(get()) }
    single { RequestGate() }
    single<TrackStore> { RoomTrackStore(get(), get(), get()) }
    single<TrackPageStore> { RoomTrackPageStore(get()) }
    single<ArtistStore> { RoomArtistStore(get(), get(), get(), get()) }
    single<FavoriteLocalDataSource> { RoomFavoriteLocalDataSource(get(), get()) }
    single<PlaylistLocalDataSource> { RoomPlaylistLocalDataSource(get()) }
    single<PlaybackHistoryLocalDataSource> { RoomPlaybackHistoryLocalDataSource(get(), get()) }
    single<TrackRepository> { TrackRepositoryImpl(get(), get(), get()) }
    single<TrackPagingRepository> { TrackPagingRepositoryImpl(get(), get()) }
    single<ArtistRepository> { ArtistRepositoryImpl(get(), get(), get()) }
    single<FavoriteRepository> { FavoriteRepositoryImpl(get()) }
    single<PlaylistRepository> { PlaylistRepositoryImpl(get()) }
    single<PlaybackHistoryRepository> { PlaybackHistoryRepositoryImpl(get()) }
}
