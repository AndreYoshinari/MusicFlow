package br.com.musicflow.feature.home

import androidx.paging.LoadState
import org.junit.Assert.assertEquals
import org.junit.Test

class PopularRowKindTest {
    @Test
    fun cachedItemsStayVisibleWhileTheNextRefreshLoads() {
        assertEquals(PopularRowKind.Ready, popularRowKind(LoadState.Loading, itemCount = 2))
        assertEquals(PopularRowKind.Ready, popularRowKind(LoadState.Error(IllegalStateException()), itemCount = 2))
    }

    @Test
    fun anEmptyRefreshDistinguishesLoadingFailureAndNothing() {
        assertEquals(PopularRowKind.Loading, popularRowKind(LoadState.Loading, itemCount = 0))
        assertEquals(PopularRowKind.Failed, popularRowKind(LoadState.Error(IllegalStateException()), itemCount = 0))
        assertEquals(
            PopularRowKind.Empty,
            popularRowKind(LoadState.NotLoading(endOfPaginationReached = true), itemCount = 0),
        )
    }
}
