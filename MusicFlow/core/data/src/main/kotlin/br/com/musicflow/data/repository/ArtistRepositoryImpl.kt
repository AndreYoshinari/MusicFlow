package br.com.musicflow.data.repository

import br.com.musicflow.common.domain.Album
import br.com.musicflow.common.domain.Artist
import br.com.musicflow.common.domain.ArtistId
import br.com.musicflow.common.domain.DomainResult
import br.com.musicflow.common.domain.repository.ArtistRepository
import br.com.musicflow.data.local.ArtistStore
import br.com.musicflow.data.local.CATALOG_MAX_AGE_MILLIS
import br.com.musicflow.data.local.SEARCH_MAX_AGE_MILLIS
import br.com.musicflow.data.mapper.toAlbumList
import br.com.musicflow.data.mapper.toArtistList
import br.com.musicflow.data.mapper.toSingleArtist
import br.com.musicflow.data.remote.JamendoArtistDataSource
import java.time.Instant

internal class ArtistRepositoryImpl(
    private val remote: JamendoArtistDataSource,
    private val local: ArtistStore,
    private val gate: RequestGate,
    private val clock: () -> Instant = Instant::now,
) : ArtistRepository {
    override suspend fun getArtistById(id: ArtistId): DomainResult<Artist> =
        gate.run("$ARTIST_PREFIX${id.value}") {
            val now = clock()
            syncCatalog(
                cached = local.readArtist(id.value, now, CATALOG_MAX_AGE_MILLIS),
                remote = { remote.getArtist(id).toSingleArtist() },
                save = { local.saveArtist(it, now) },
            )
        }

    override suspend fun getFeaturedArtists(): DomainResult<List<Artist>> =
        gate.run(FEATURED) {
            val now = clock()
            syncCatalog(
                cached = local.readArtists(FEATURED, now, CATALOG_MAX_AGE_MILLIS),
                remote = { remote.getFeaturedArtists().toArtistList() },
                save = { local.saveArtists(FEATURED, it, now) },
            )
        }

    override suspend fun searchArtists(query: String): DomainResult<List<Artist>> {
        val key = "$SEARCH_PREFIX$query"
        return gate.run(key) {
            val now = clock()
            syncCatalog(
                cached = local.readArtists(key, now, SEARCH_MAX_AGE_MILLIS),
                remote = { remote.searchArtists(query).toArtistList() },
                save = { local.saveArtists(key, it, now) },
            )
        }
    }

    override suspend fun getAlbumsByArtist(artistId: ArtistId): DomainResult<List<Album>> {
        val key = "$ALBUMS_PREFIX${artistId.value}"
        return gate.run(key) {
            val now = clock()
            syncCatalog(
                cached = local.readAlbums(key, now, CATALOG_MAX_AGE_MILLIS),
                remote = { remote.getAlbums(artistId).toAlbumList() },
                save = { local.saveAlbums(key, it, now) },
            )
        }
    }

    override suspend fun getPopularAlbums(): DomainResult<List<Album>> =
        gate.run(POPULAR_ALBUMS) {
            val now = clock()
            syncCatalog(
                cached = local.readAlbums(POPULAR_ALBUMS, now, CATALOG_MAX_AGE_MILLIS),
                remote = { remote.getPopularAlbums().toAlbumList() },
                save = { local.saveAlbums(POPULAR_ALBUMS, it, now) },
            )
        }

    private companion object {
        const val ARTIST_PREFIX = "artist:"
        const val SEARCH_PREFIX = "artists:search:"
        const val FEATURED = "artists:featured"
        const val ALBUMS_PREFIX = "albums:artist:"
        const val POPULAR_ALBUMS = "albums:popular"
    }
}
