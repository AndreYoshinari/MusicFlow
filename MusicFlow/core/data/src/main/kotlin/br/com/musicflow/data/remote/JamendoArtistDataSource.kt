package br.com.musicflow.data.remote

import br.com.musicflow.common.domain.ArtistId
import br.com.musicflow.network.JamendoClient
import br.com.musicflow.network.NetworkResult
import br.com.musicflow.network.dto.JamendoAlbumDto
import br.com.musicflow.network.dto.JamendoArtistDto

internal class JamendoArtistDataSource(
    private val client: JamendoClient,
) {
    suspend fun getArtist(id: ArtistId): NetworkResult<List<JamendoArtistDto>> =
        client.getArtists(id = id.value.toString())

    suspend fun searchArtists(query: String): NetworkResult<List<JamendoArtistDto>> =
        client.getArtists(limit = PAGE_LIMIT, nameSearch = query)

    suspend fun getFeaturedArtists(): NetworkResult<List<JamendoArtistDto>> =
        client.getArtists(limit = PAGE_LIMIT, order = POPULAR_ORDER)

    suspend fun getAlbums(artistId: ArtistId): NetworkResult<List<JamendoAlbumDto>> =
        client.getAlbums(
            limit = PAGE_LIMIT,
            order = RELEASE_DATE_DESCENDING,
            artistId = artistId.value.toString(),
        )

    suspend fun getPopularAlbums(): NetworkResult<List<JamendoAlbumDto>> =
        client.getAlbums(limit = PAGE_LIMIT, order = POPULAR_ORDER)

    private companion object {
        const val PAGE_LIMIT = 20
        const val RELEASE_DATE_DESCENDING = "releasedate_desc"
        // Rating orders already default to descending in the Jamendo API.
        const val POPULAR_ORDER = "popularity_total"
    }
}
