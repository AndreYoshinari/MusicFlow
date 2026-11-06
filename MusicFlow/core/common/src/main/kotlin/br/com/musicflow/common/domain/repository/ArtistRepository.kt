package br.com.musicflow.common.domain.repository

import br.com.musicflow.common.domain.Album
import br.com.musicflow.common.domain.Artist
import br.com.musicflow.common.domain.ArtistId
import br.com.musicflow.common.domain.DomainResult

interface ArtistRepository {
    suspend fun getArtistById(id: ArtistId): DomainResult<Artist>

    suspend fun searchArtists(query: String): DomainResult<List<Artist>>

    suspend fun getFeaturedArtists(): DomainResult<List<Artist>>

    suspend fun getAlbumsByArtist(artistId: ArtistId): DomainResult<List<Album>>

    suspend fun getPopularAlbums(): DomainResult<List<Album>>
}
