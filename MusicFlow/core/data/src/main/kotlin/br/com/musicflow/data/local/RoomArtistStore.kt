package br.com.musicflow.data.local

import br.com.musicflow.common.domain.Album
import br.com.musicflow.common.domain.AlbumId
import br.com.musicflow.common.domain.Artist
import br.com.musicflow.common.domain.ArtistId
import br.com.musicflow.common.domain.ArtistRef
import br.com.musicflow.common.domain.ContentSource
import br.com.musicflow.database.dao.CachedAlbumDao
import br.com.musicflow.database.dao.CachedArtistDao
import br.com.musicflow.database.dao.CatalogCacheDao
import br.com.musicflow.database.dao.CatalogSnapshotDao
import br.com.musicflow.database.entity.CachedAlbumEntity
import br.com.musicflow.database.entity.CachedArtistEntity
import br.com.musicflow.database.entity.CatalogSnapshotEntity
import java.time.Instant
import java.time.LocalDate

internal class RoomArtistStore(
    private val artists: CachedArtistDao,
    private val albums: CachedAlbumDao,
    private val snapshots: CatalogSnapshotDao,
    private val cache: CatalogCacheDao,
) : ArtistStore {
    override suspend fun readArtist(id: Long, now: Instant, maxAgeMillis: Long): CatalogRead<Artist> {
        val entity = artists.getById(id) ?: return CatalogRead.Missing
        return classify(entity.cachedAt, now, maxAgeMillis, entity.toArtist())
    }

    override suspend fun readArtists(
        key: String,
        now: Instant,
        maxAgeMillis: Long,
    ): CatalogRead<List<Artist>> {
        val snapshot = snapshots.get(key) ?: return CatalogRead.Missing
        val ids = decodeIds(snapshot.ids)
        val ordered = loadArtists(ids) ?: return CatalogRead.Missing
        return classify(snapshot.fetchedAt, now, maxAgeMillis, ordered)
    }

    override suspend fun readAlbums(key: String, now: Instant, maxAgeMillis: Long): CatalogRead<List<Album>> {
        val snapshot = snapshots.get(key) ?: return CatalogRead.Missing
        val ids = decodeIds(snapshot.ids)
        val ordered = loadAlbums(ids) ?: return CatalogRead.Missing
        return classify(snapshot.fetchedAt, now, maxAgeMillis, ordered)
    }

    override suspend fun saveArtist(artist: Artist, observedAt: Instant) {
        artists.upsertAll(listOf(artist.toEntity(observedAt)))
    }

    override suspend fun saveArtists(key: String, artists: List<Artist>, observedAt: Instant) {
        cache.saveArtists(
            artists.map { it.toEntity(observedAt) },
            snapshot(key, artists.map { it.id.value }, observedAt),
        )
    }

    override suspend fun saveAlbums(key: String, albums: List<Album>, observedAt: Instant) {
        cache.saveAlbums(
            albums.map { it.toEntity(observedAt) },
            snapshot(key, albums.map { it.id.value }, observedAt),
        )
    }

    private suspend fun loadArtists(ids: List<Long>): List<Artist>? {
        if (ids.isEmpty()) return emptyList()
        val stored = artists.getByIds(ids).associateBy { it.id }
        if (!stored.keys.containsAll(ids)) return null
        return ids.map { stored.getValue(it).toArtist() }
    }

    private suspend fun loadAlbums(ids: List<Long>): List<Album>? {
        if (ids.isEmpty()) return emptyList()
        val stored = albums.getByIds(ids).associateBy { it.id }
        if (!stored.keys.containsAll(ids)) return null
        return ids.map { stored.getValue(it).toAlbum() }
    }
}

private fun snapshot(key: String, ids: List<Long>, observedAt: Instant) = CatalogSnapshotEntity(
    key = key,
    ids = ids.joinToString(","),
    fetchedAt = observedAt.toEpochMilli(),
)

private fun Artist.toEntity(observedAt: Instant) = CachedArtistEntity(
    id = id.value,
    name = name,
    imageUrl = imageUrl,
    provider = source.provider,
    pageUrl = source.pageUrl,
    cachedAt = observedAt.toEpochMilli(),
)

private fun CachedArtistEntity.toArtist() = Artist(
    id = ArtistId(id),
    name = name,
    imageUrl = imageUrl?.takeIf { it.isNotBlank() },
    source = ContentSource(provider, pageUrl),
)

private fun Album.toEntity(observedAt: Instant) = CachedAlbumEntity(
    id = id.value,
    title = title,
    artistId = artist.id.value,
    artistName = artist.name,
    artworkUrl = artworkUrl,
    releaseDate = releaseDate?.toString(),
    provider = source.provider,
    pageUrl = source.pageUrl,
    cachedAt = observedAt.toEpochMilli(),
)

private fun CachedAlbumEntity.toAlbum() = Album(
    id = AlbumId(id),
    title = title,
    artist = ArtistRef(ArtistId(artistId), artistName),
    artworkUrl = artworkUrl?.takeIf { it.isNotBlank() },
    releaseDate = releaseDate?.takeIf { it.isNotBlank() }?.let(LocalDate::parse),
    source = ContentSource(provider, pageUrl),
)

private fun <T> classify(observedAt: Long, now: Instant, maxAgeMillis: Long, value: T): CatalogRead<T> =
    if (isFresh(observedAt, now.toEpochMilli(), maxAgeMillis)) {
        CatalogRead.Fresh(value)
    } else {
        CatalogRead.Stale(value)
    }

private fun decodeIds(ids: String): List<Long> =
    if (ids.isBlank()) emptyList() else ids.split(",").map { it.toLong() }
