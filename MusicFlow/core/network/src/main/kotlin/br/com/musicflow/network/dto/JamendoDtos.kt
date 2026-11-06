package br.com.musicflow.network.dto

import br.com.musicflow.network.JamendoScalarStringSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class JamendoResponse<T>(
    val headers: JamendoHeaders = JamendoHeaders(),
    val results: List<T> = emptyList(),
)

@Serializable
data class JamendoHeaders(
    val status: String = "",
    val code: Int = UNKNOWN_CODE,
    @SerialName("error_message")
    val errorMessage: String = "",
    val warnings: String = "",
    @SerialName("results_count")
    val resultsCount: Int = 0,
) {
    private companion object {
        const val UNKNOWN_CODE = -1
    }
}

@Serializable
data class JamendoTrackDto(
    @Serializable(with = JamendoScalarStringSerializer::class)
    val id: String = "",
    val name: String = "",
    @Serializable(with = JamendoScalarStringSerializer::class)
    val duration: String = "",
    @Serializable(with = JamendoScalarStringSerializer::class)
    @SerialName("artist_id")
    val artistId: String = "",
    @SerialName("artist_name")
    val artistName: String = "",
    @SerialName("album_name")
    val albumName: String = "",
    @Serializable(with = JamendoScalarStringSerializer::class)
    @SerialName("album_id")
    val albumId: String = "",
    @SerialName("license_ccurl")
    val licenseUrl: String = "",
    @SerialName("releasedate")
    val releaseDate: String = "",
    @SerialName("album_image")
    val albumImage: String = "",
    val audio: String = "",
    val image: String = "",
    @SerialName("shareurl")
    val shareUrl: String = "",
)

@Serializable
data class JamendoArtistDto(
    @Serializable(with = JamendoScalarStringSerializer::class)
    val id: String = "",
    val name: String = "",
    val image: String = "",
    @SerialName("shareurl")
    val shareUrl: String = "",
)

@Serializable
data class JamendoAlbumDto(
    @Serializable(with = JamendoScalarStringSerializer::class)
    val id: String = "",
    val name: String = "",
    @SerialName("releasedate")
    val releaseDate: String = "",
    @Serializable(with = JamendoScalarStringSerializer::class)
    @SerialName("artist_id")
    val artistId: String = "",
    @SerialName("artist_name")
    val artistName: String = "",
    val image: String = "",
    @SerialName("shareurl")
    val shareUrl: String = "",
)
