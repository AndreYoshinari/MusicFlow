package br.com.musicflow.feature.search

import androidx.compose.runtime.Immutable
import br.com.musicflow.common.domain.Artist
import br.com.musicflow.common.domain.DomainError
import br.com.musicflow.common.domain.DomainResult

@Immutable
internal data class SearchArtistModel(
    val id: Long,
    val name: String,
    val imageUrl: String?,
)

@Immutable
internal sealed interface ArtistMatches {
    data object Hidden : ArtistMatches

    data object Loading : ArtistMatches

    data class Ready(val artists: List<SearchArtistModel>) : ArtistMatches

    data class Failed(val error: DomainError) : ArtistMatches
}

@Immutable
internal data class SearchUiState(
    val query: String = "",
    val artists: ArtistMatches = ArtistMatches.Hidden,
)

internal fun Artist.toSearchModel(): SearchArtistModel = SearchArtistModel(
    id = id.value,
    name = name,
    imageUrl = imageUrl,
)

internal fun DomainResult<List<Artist>>.toMatches(): ArtistMatches = when (this) {
    is DomainResult.Failure -> ArtistMatches.Failed(error)
    is DomainResult.Success -> {
        if (value.isEmpty()) {
            ArtistMatches.Hidden
        } else {
            ArtistMatches.Ready(value.map { it.toSearchModel() })
        }
    }
}
