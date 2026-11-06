@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package br.com.musicflow.common.domain.usecase

import androidx.paging.PagingData
import br.com.musicflow.common.domain.ArtistId
import br.com.musicflow.common.domain.Track
import br.com.musicflow.common.domain.repository.TrackPagingRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ObserveTrackSearchUseCaseTest {
    @Test
    fun debounceKeepsOnlyTheLatestTerm() = runTest {
        val paging = FakeTrackPaging()
        val queries = MutableSharedFlow<String>(extraBufferCapacity = 8)
        val job = collectSearch(paging, queries)

        queries.emit("au")
        queries.emit("aurora")
        advanceTimeBy(TRACK_SEARCH_DEBOUNCE_MILLIS - 1)
        runCurrent()
        assertTrue(paging.searches.isEmpty())

        advanceTimeBy(1)
        runCurrent()
        assertEquals(listOf("aurora"), paging.searches)
        job.cancel()
    }

    @Test
    fun theSameTermDoesNotStartAnotherSearch() = runTest {
        val paging = FakeTrackPaging()
        val queries = MutableSharedFlow<String>(extraBufferCapacity = 8)
        val job = collectSearch(paging, queries)

        queries.emit("aurora")
        advanceTimeBy(TRACK_SEARCH_DEBOUNCE_MILLIS)
        runCurrent()
        queries.emit("  aurora  ")
        advanceTimeBy(TRACK_SEARCH_DEBOUNCE_MILLIS)
        runCurrent()

        assertEquals(listOf("aurora"), paging.searches)
        job.cancel()
    }

    @Test
    fun aBlankTermDoesNotCallTheRepositoryAndCancelsThePreviousSearch() = runTest {
        val paging = FakeTrackPaging()
        val queries = MutableSharedFlow<String>(extraBufferCapacity = 8)
        val job = collectSearch(paging, queries)

        queries.emit("   ")
        advanceTimeBy(TRACK_SEARCH_DEBOUNCE_MILLIS)
        runCurrent()
        assertTrue(paging.searches.isEmpty())

        queries.emit("aurora")
        advanceTimeBy(TRACK_SEARCH_DEBOUNCE_MILLIS)
        runCurrent()
        queries.emit(" ")
        runCurrent()

        assertEquals(listOf("aurora"), paging.searches)
        assertEquals(listOf("aurora"), paging.cancelled)
        job.cancel()
    }

    @Test
    fun aNewTermCancelsThePreviousPageAndStartsAtANewQuery() = runTest {
        val paging = FakeTrackPaging()
        val queries = MutableSharedFlow<String>(extraBufferCapacity = 8)
        val job = collectSearch(paging, queries)

        queries.emit("aurora")
        advanceTimeBy(TRACK_SEARCH_DEBOUNCE_MILLIS)
        runCurrent()
        queries.emit("noite")
        advanceTimeBy(TRACK_SEARCH_DEBOUNCE_MILLIS - 1)
        runCurrent()
        assertEquals(listOf("aurora"), paging.searches)

        advanceTimeBy(1)
        runCurrent()
        assertEquals(listOf("aurora", "noite"), paging.searches)
        assertEquals(listOf("aurora"), paging.cancelled)
        job.cancel()
    }
}

private fun TestScope.collectSearch(
    paging: FakeTrackPaging,
    queries: Flow<String>,
): Job {
    val job = backgroundScope.launch {
        ObserveTrackSearchUseCase(paging)(queries).collect { }
    }
    runCurrent()
    return job
}

private class FakeTrackPaging : TrackPagingRepository {
    val searches = mutableListOf<String>()
    val cancelled = mutableListOf<String>()

    override fun popularTracks(): Flow<PagingData<Track>> = emptyFlow()

    override fun tracksByArtist(artistId: ArtistId): Flow<PagingData<Track>> = emptyFlow()

    override fun searchTracks(query: String): Flow<PagingData<Track>> = callbackFlow {
        searches += query
        trySend(PagingData.empty())
        awaitClose { cancelled += query }
    }
}
