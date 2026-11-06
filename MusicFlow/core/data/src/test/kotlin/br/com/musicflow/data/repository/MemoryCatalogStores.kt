package br.com.musicflow.data.repository

import br.com.musicflow.common.domain.Album
import br.com.musicflow.common.domain.Artist
import br.com.musicflow.common.domain.Track
import br.com.musicflow.data.local.ArtistStore
import br.com.musicflow.data.local.CatalogRead
import br.com.musicflow.data.local.STREAM_URL_MAX_AGE_MILLIS
import br.com.musicflow.data.local.TrackStore
import br.com.musicflow.data.local.isFresh
import java.time.Instant

internal class MemoryTrackStore : TrackStore {
    private val tracks = mutableMapOf<Long, Pair<Track, Instant>>()
    private val snapshots = mutableMapOf<String, Pair<List<Long>, Instant>>()

    override suspend fun read(key: String, now: Instant, maxAgeMillis: Long): CatalogRead<List<Track>> {
        val snapshot = snapshots[key] ?: return CatalogRead.Missing
        val ordered = snapshot.first.mapNotNull { id -> tracks[id]?.first?.at(tracks[id]!!.second, now) }
        if (ordered.size != snapshot.first.size) return CatalogRead.Missing
        return classify(snapshot.second, now, maxAgeMillis, ordered)
    }

    override suspend fun readTrack(id: Long, now: Instant, maxAgeMillis: Long): CatalogRead<Track> {
        val stored = tracks[id] ?: return CatalogRead.Missing
        return classify(stored.second, now, maxAgeMillis, stored.first.at(stored.second, now))
    }

    override suspend fun freshStreamUrl(id: Long, now: Instant): String? {
        val stored = tracks[id] ?: return null
        if (!isFresh(stored.second.toEpochMilli(), now.toEpochMilli(), STREAM_URL_MAX_AGE_MILLIS)) return null
        return stored.first.streamUrl
    }

    override suspend fun save(key: String, tracks: List<Track>, observedAt: Instant) {
        tracks.forEach { this.tracks[it.id.value] = it to observedAt }
        snapshots[key] = tracks.map { it.id.value } to observedAt
    }

    override suspend fun saveTrack(track: Track, observedAt: Instant) {
        tracks[track.id.value] = track to observedAt
    }
}

internal class MemoryArtistStore : ArtistStore {
    private val artists = mutableMapOf<Long, Pair<Artist, Instant>>()
    private val albums = mutableMapOf<Long, Pair<Album, Instant>>()
    private val artistSnapshots = mutableMapOf<String, Pair<List<Long>, Instant>>()
    private val albumSnapshots = mutableMapOf<String, Pair<List<Long>, Instant>>()

    override suspend fun readArtist(id: Long, now: Instant, maxAgeMillis: Long): CatalogRead<Artist> {
        val stored = artists[id] ?: return CatalogRead.Missing
        return classify(stored.second, now, maxAgeMillis, stored.first)
    }

    override suspend fun readArtists(key: String, now: Instant, maxAgeMillis: Long): CatalogRead<List<Artist>> {
        val snapshot = artistSnapshots[key] ?: return CatalogRead.Missing
        val ordered = snapshot.first.mapNotNull { artists[it]?.first }
        if (ordered.size != snapshot.first.size) return CatalogRead.Missing
        return classify(snapshot.second, now, maxAgeMillis, ordered)
    }

    override suspend fun readAlbums(key: String, now: Instant, maxAgeMillis: Long): CatalogRead<List<Album>> {
        val snapshot = albumSnapshots[key] ?: return CatalogRead.Missing
        val ordered = snapshot.first.mapNotNull { albums[it]?.first }
        if (ordered.size != snapshot.first.size) return CatalogRead.Missing
        return classify(snapshot.second, now, maxAgeMillis, ordered)
    }

    override suspend fun saveArtist(artist: Artist, observedAt: Instant) {
        artists[artist.id.value] = artist to observedAt
    }

    override suspend fun saveArtists(key: String, artists: List<Artist>, observedAt: Instant) {
        artists.forEach { this.artists[it.id.value] = it to observedAt }
        artistSnapshots[key] = artists.map { it.id.value } to observedAt
    }

    override suspend fun saveAlbums(key: String, albums: List<Album>, observedAt: Instant) {
        albums.forEach { this.albums[it.id.value] = it to observedAt }
        albumSnapshots[key] = albums.map { it.id.value } to observedAt
    }
}

private fun Track.at(observedAt: Instant, now: Instant): Track {
    val stream = streamUrl?.takeIf {
        isFresh(observedAt.toEpochMilli(), now.toEpochMilli(), STREAM_URL_MAX_AGE_MILLIS)
    }
    return copy(streamUrl = stream)
}

private fun <T> classify(observedAt: Instant, now: Instant, maxAgeMillis: Long, value: T): CatalogRead<T> =
    if (isFresh(observedAt.toEpochMilli(), now.toEpochMilli(), maxAgeMillis)) {
        CatalogRead.Fresh(value)
    } else {
        CatalogRead.Stale(value)
    }
