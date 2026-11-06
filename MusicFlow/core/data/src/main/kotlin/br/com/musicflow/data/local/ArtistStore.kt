package br.com.musicflow.data.local

import br.com.musicflow.common.domain.Album
import br.com.musicflow.common.domain.Artist
import java.time.Instant

internal interface ArtistStore {
    suspend fun readArtist(id: Long, now: Instant, maxAgeMillis: Long): CatalogRead<Artist>

    suspend fun readArtists(key: String, now: Instant, maxAgeMillis: Long): CatalogRead<List<Artist>>

    suspend fun readAlbums(key: String, now: Instant, maxAgeMillis: Long): CatalogRead<List<Album>>

    suspend fun saveArtist(artist: Artist, observedAt: Instant)

    suspend fun saveArtists(key: String, artists: List<Artist>, observedAt: Instant)

    suspend fun saveAlbums(key: String, albums: List<Album>, observedAt: Instant)
}
