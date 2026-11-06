package br.com.musicflow.network.api

import br.com.musicflow.network.dto.JamendoAlbumDto
import br.com.musicflow.network.dto.JamendoArtistDto
import br.com.musicflow.network.dto.JamendoResponse
import br.com.musicflow.network.dto.JamendoTrackDto
import retrofit2.http.GET
import retrofit2.http.Query

interface JamendoApiService {
    @GET("tracks")
    suspend fun getTracks(
        @Query("format") format: String = "json",
        @Query("limit") limit: Int? = null,
        @Query("offset") offset: Int? = null,
        @Query("order") order: String? = null,
        @Query("id") id: String? = null,
        @Query("artist_id") artistId: String? = null,
        @Query("search") search: String? = null,
        @Query("imagesize") imageSize: Int? = null,
        @Query("audioformat") audioFormat: String? = null,
    ): JamendoResponse<JamendoTrackDto>

    @GET("artists")
    suspend fun getArtists(
        @Query("format") format: String = "json",
        @Query("limit") limit: Int? = null,
        @Query("order") order: String? = null,
        @Query("id") id: String? = null,
        @Query("namesearch") nameSearch: String? = null,
    ): JamendoResponse<JamendoArtistDto>

    @GET("albums")
    suspend fun getAlbums(
        @Query("format") format: String = "json",
        @Query("limit") limit: Int? = null,
        @Query("order") order: String? = null,
        @Query("artist_id") artistId: String? = null,
        @Query("imagesize") imageSize: Int? = null,
    ): JamendoResponse<JamendoAlbumDto>
}
