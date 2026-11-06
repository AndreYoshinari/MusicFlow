package br.com.musicflow.data.repository

import br.com.musicflow.common.domain.ArtistId
import br.com.musicflow.common.domain.DomainError
import br.com.musicflow.common.domain.DomainResult
import br.com.musicflow.common.domain.Track
import br.com.musicflow.common.domain.TrackId
import br.com.musicflow.common.domain.repository.TrackRepository
import br.com.musicflow.data.local.CATALOG_MAX_AGE_MILLIS
import br.com.musicflow.data.local.SEARCH_MAX_AGE_MILLIS
import br.com.musicflow.data.local.TrackStore
import br.com.musicflow.data.mapper.toSingleTrack
import br.com.musicflow.data.mapper.toTrackList
import br.com.musicflow.data.remote.JamendoTrackDataSource
import java.time.Instant

internal class TrackRepositoryImpl(
    private val remote: JamendoTrackDataSource,
    private val local: TrackStore,
    private val gate: RequestGate,
    private val clock: () -> Instant = Instant::now,
) : TrackRepository {
    override suspend fun getPopularTracks(): DomainResult<List<Track>> = gate.run(POPULAR) {
        val now = clock()
        syncCatalog(
            cached = local.read(POPULAR, now, CATALOG_MAX_AGE_MILLIS),
            remote = { remote.getPopularTracks().toTrackList() },
            save = { local.save(POPULAR, it, now) },
        )
    }

    override suspend fun searchTracks(query: String): DomainResult<List<Track>> {
        val key = "$SEARCH_PREFIX$query"
        return gate.run(key) {
            val now = clock()
            syncCatalog(
                cached = local.read(key, now, SEARCH_MAX_AGE_MILLIS),
                remote = { remote.searchTracks(query).toTrackList() },
                save = { local.save(key, it, now) },
            )
        }
    }

    override suspend fun getTrackById(id: TrackId): DomainResult<Track> = gate.run("$TRACK_PREFIX${id.value}") {
        val now = clock()
        syncCatalog(
            cached = local.readTrack(id.value, now, CATALOG_MAX_AGE_MILLIS),
            remote = { remote.getTrack(id).toSingleTrack() },
            save = { local.saveTrack(it, now) },
        )
    }

    override suspend fun getTracksByArtist(artistId: ArtistId): DomainResult<List<Track>> {
        val key = "$ARTIST_TRACKS_PREFIX${artistId.value}"
        return gate.run(key) {
            val now = clock()
            syncCatalog(
                cached = local.read(key, now, CATALOG_MAX_AGE_MILLIS),
                remote = { remote.getTracksByArtist(artistId).toTrackList() },
                save = { local.save(key, it, now) },
            )
        }
    }

    override suspend fun getTrackStreamUrl(id: TrackId): DomainResult<String> =
        gate.run("$STREAM_PREFIX${id.value}") {
            val now = clock()
            local.freshStreamUrl(id.value, now)?.let { return@run DomainResult.Success(it) }
            when (val result = remote.getTrack(id).toSingleTrack()) {
                is DomainResult.Failure -> result
                is DomainResult.Success -> {
                    local.saveTrack(result.value, now)
                    result.value.streamUrl?.let { DomainResult.Success(it) }
                        ?: DomainResult.Failure(DomainError.StreamUnavailable)
                }
            }
        }

    private companion object {
        const val POPULAR = "tracks:popular"
        const val SEARCH_PREFIX = "tracks:search:"
        const val TRACK_PREFIX = "track:"
        const val ARTIST_TRACKS_PREFIX = "tracks:artist:"
        const val STREAM_PREFIX = "stream:"
    }
}
