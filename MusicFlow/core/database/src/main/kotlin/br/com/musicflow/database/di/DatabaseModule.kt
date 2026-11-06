package br.com.musicflow.database.di

import androidx.room.Room
import br.com.musicflow.database.MusicFlowDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

// The single is lazy. The database file opens on the first query, not during startKoin.
val databaseModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            MusicFlowDatabase::class.java,
            MusicFlowDatabase.NAME,
        ).addMigrations(*MusicFlowDatabase.MIGRATIONS)
            .build()
    }
    single { get<MusicFlowDatabase>().cachedTrackDao() }
    single { get<MusicFlowDatabase>().cachedArtistDao() }
    single { get<MusicFlowDatabase>().cachedAlbumDao() }
    single { get<MusicFlowDatabase>().catalogSnapshotDao() }
    single { get<MusicFlowDatabase>().catalogCacheDao() }
    single { get<MusicFlowDatabase>().catalogPageDao() }
    single { get<MusicFlowDatabase>().favoriteDao() }
    single { get<MusicFlowDatabase>().playlistDao() }
    single { get<MusicFlowDatabase>().playbackHistoryDao() }
}
