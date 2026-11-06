package br.com.musicflow.common.domain.repository

import androidx.paging.PagingData
import br.com.musicflow.common.domain.ArtistId
import br.com.musicflow.common.domain.Track
import kotlinx.coroutines.flow.Flow

interface TrackPagingRepository {
    fun popularTracks(): Flow<PagingData<Track>>

    fun searchTracks(query: String): Flow<PagingData<Track>>

    fun tracksByArtist(artistId: ArtistId): Flow<PagingData<Track>>
}
