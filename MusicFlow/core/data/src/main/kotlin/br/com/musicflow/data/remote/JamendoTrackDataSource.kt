package br.com.musicflow.data.remote

import br.com.musicflow.common.domain.ArtistId
import br.com.musicflow.common.domain.TrackId
import br.com.musicflow.data.local.CATALOG_PAGE_SIZE
import br.com.musicflow.network.JamendoClient
import br.com.musicflow.network.NetworkResult
import br.com.musicflow.network.dto.JamendoTrackDto

internal class JamendoTrackDataSource(
    private val client: JamendoClient,
) {
    suspend fun getPopularTracks(offset: Int = 0): NetworkResult<List<JamendoTrackDto>> =
        client.getTracks(limit = CATALOG_PAGE_SIZE, offset = offset, order = POPULAR_ORDER)

    // Omitting order keeps Jamendo's default relevance. Another order drops it.
    suspend fun searchTracks(query: String, offset: Int = 0): NetworkResult<List<JamendoTrackDto>> =
        client.getTracks(limit = CATALOG_PAGE_SIZE, offset = offset, search = query)

    suspend fun getTrack(id: TrackId): NetworkResult<List<JamendoTrackDto>> =
        client.getTracks(id = id.value.toString())

    suspend fun getTracksByArtist(artistId: ArtistId, offset: Int = 0): NetworkResult<List<JamendoTrackDto>> =
        client.getTracks(
            limit = CATALOG_PAGE_SIZE,
            offset = offset,
            order = POPULAR_ORDER,
            artistId = artistId.value.toString(),
        )

    private companion object {
        // Rating orders already default to descending in the Jamendo API.
        const val POPULAR_ORDER = "popularity_total"
    }
}
