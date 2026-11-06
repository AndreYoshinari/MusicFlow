package br.com.musicflow.common.domain.repository

import br.com.musicflow.common.domain.ArtistId
import br.com.musicflow.common.domain.DomainResult
import br.com.musicflow.common.domain.Track
import br.com.musicflow.common.domain.TrackId

interface TrackRepository {
    suspend fun getPopularTracks(): DomainResult<List<Track>>

    suspend fun searchTracks(query: String): DomainResult<List<Track>>

    suspend fun getTrackById(id: TrackId): DomainResult<Track>

    suspend fun getTracksByArtist(artistId: ArtistId): DomainResult<List<Track>>

    suspend fun getTrackStreamUrl(id: TrackId): DomainResult<String>
}
