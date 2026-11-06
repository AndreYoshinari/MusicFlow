package br.com.musicflow.feature.search

import androidx.paging.LoadState
import br.com.musicflow.common.domain.DomainError
import br.com.musicflow.common.domain.PageLoadException

internal enum class SearchResultsKind {
    Idle,
    Loading,
    Empty,
    Failed,
    Ready,
}

internal fun searchResultsKind(query: String, refresh: LoadState, itemCount: Int): SearchResultsKind {
    if (query.isBlank()) return SearchResultsKind.Idle
    if (itemCount > 0) return SearchResultsKind.Ready
    return when (refresh) {
        is LoadState.Loading -> SearchResultsKind.Loading
        is LoadState.Error -> SearchResultsKind.Failed
        is LoadState.NotLoading -> SearchResultsKind.Empty
    }
}

internal fun searchFailure(refresh: LoadState): DomainError {
    val error = (refresh as? LoadState.Error)?.error
    return (error as? PageLoadException)?.error ?: DomainError.Unavailable
}
