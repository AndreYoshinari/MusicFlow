package br.com.musicflow.feature.search

import androidx.paging.LoadState
import br.com.musicflow.common.domain.DomainError
import br.com.musicflow.common.domain.PageLoadException
import org.junit.Assert.assertEquals
import org.junit.Test

class SearchResultsKindTest {
    @Test
    fun blankQueryStaysIdle() {
        assertEquals(
            SearchResultsKind.Idle,
            searchResultsKind("   ", LoadState.NotLoading(endOfPaginationReached = true), itemCount = 0),
        )
    }

    @Test
    fun refreshStatesMapToLoadingEmptyAndError() {
        assertEquals(SearchResultsKind.Loading, searchResultsKind("aurora", LoadState.Loading, 0))
        assertEquals(
            SearchResultsKind.Empty,
            searchResultsKind("aurora", LoadState.NotLoading(endOfPaginationReached = true), 0),
        )
        val failed = LoadState.Error(PageLoadException(DomainError.Offline))
        assertEquals(SearchResultsKind.Failed, searchResultsKind("aurora", failed, 0))
        assertEquals(DomainError.Offline, searchFailure(failed))
    }

    @Test
    fun cachedTracksStayVisibleWhenRefreshFails() {
        val failed = LoadState.Error(PageLoadException(DomainError.Unavailable))
        assertEquals(SearchResultsKind.Ready, searchResultsKind("aurora", failed, itemCount = 4))
        assertEquals(DomainError.Unavailable, searchFailure(failed))
        assertEquals(DomainError.Unavailable, searchFailure(LoadState.Error(IllegalStateException("x"))))
    }
}
