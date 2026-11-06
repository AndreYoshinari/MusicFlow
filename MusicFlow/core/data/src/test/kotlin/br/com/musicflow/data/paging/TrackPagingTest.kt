@file:OptIn(
    androidx.paging.ExperimentalPagingApi::class,
    kotlinx.coroutines.ExperimentalCoroutinesApi::class,
)

package br.com.musicflow.data.paging

import androidx.paging.LoadState
import androidx.paging.LoadType
import androidx.paging.PagingConfig
import androidx.paging.PagingDataPresenter
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import br.com.musicflow.common.domain.AlbumId
import br.com.musicflow.common.domain.AlbumRef
import br.com.musicflow.common.domain.ArtistId
import br.com.musicflow.common.domain.ArtistRef
import br.com.musicflow.common.domain.ContentSource
import br.com.musicflow.common.domain.DomainError
import br.com.musicflow.common.domain.DomainResult
import br.com.musicflow.common.domain.PageLoadException
import br.com.musicflow.common.domain.Track
import br.com.musicflow.common.domain.TrackId
import br.com.musicflow.data.local.CATALOG_MAX_AGE_MILLIS
import br.com.musicflow.data.remote.JamendoTrackDataSource
import br.com.musicflow.data.repository.TrackPagingRepositoryImpl
import br.com.musicflow.network.NetworkResult
import br.com.musicflow.network.dto.JamendoTrackDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class TrackPagingTest {
    private val now = Instant.parse("2026-11-05T18:00:00Z")

    @Test
    fun theFirstPageIsStoredFromOffsetZero() = runTest {
        val store = MemoryTrackPageStore()
        val mediator = mediator(store) { offset ->
            assertEquals(0, offset)
            DomainResult.Success(listOf(track(1), track(2)))
        }

        val result = mediator.load(LoadType.REFRESH, pagingState())

        assertTrue(result is RemoteMediator.MediatorResult.Success)
        assertEquals(false, (result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
        assertEquals(listOf(1L, 2L), store.tracks("tracks:popular").map { it.id.value })
    }

    @Test
    fun theNextPageContinuesAtTheNextOffset() = runTest {
        val store = MemoryTrackPageStore()
        val offsets = mutableListOf<Int>()
        val mediator = mediator(store) { offset ->
            offsets += offset
            val page = if (offset == 0) listOf(track(1), track(2)) else listOf(track(3))
            DomainResult.Success(page)
        }

        mediator.load(LoadType.REFRESH, pagingState())
        val second = mediator.load(LoadType.APPEND, pagingState())

        assertEquals(listOf(0, 2), offsets)
        assertTrue((second as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
        assertEquals(listOf(1L, 2L, 3L), store.tracks("tracks:popular").map { it.id.value })
    }

    @Test
    fun anEmptyPageEndsTheCatalog() = runTest {
        val store = MemoryTrackPageStore()
        var calls = 0
        val mediator = mediator(store) {
            calls += 1
            DomainResult.Success(emptyList())
        }

        val result = mediator.load(LoadType.REFRESH, pagingState())
        mediator.load(LoadType.APPEND, pagingState())

        assertTrue((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
        assertTrue(store.tracks("tracks:popular").isEmpty())
        assertEquals(1, calls)
    }

    @Test
    fun aNetworkFailureStaysAnErrorAndKeepsTheStoredPage() = runTest {
        val store = MemoryTrackPageStore()
        val script = mutableListOf(
            DomainResult.Success(listOf(track(1), track(2))),
            DomainResult.Failure(DomainError.Offline),
        )
        val mediator = mediator(store) { script.removeAt(0) }
        mediator.load(LoadType.REFRESH, pagingState())

        val failed = mediator.load(LoadType.REFRESH, pagingState())

        val error = (failed as RemoteMediator.MediatorResult.Error).throwable as PageLoadException
        assertEquals(DomainError.Offline, error.error)
        assertEquals(listOf(1L, 2L), store.tracks("tracks:popular").map { it.id.value })
    }

    @Test
    fun retryRepeatsTheSameOffset() = runTest {
        val store = MemoryTrackPageStore()
        val offsets = mutableListOf<Int>()
        val script = mutableListOf(
            DomainResult.Success(listOf(track(1), track(2))),
            DomainResult.Failure(DomainError.Timeout),
            DomainResult.Success(listOf(track(3))),
        )
        val mediator = mediator(store) { offset ->
            offsets += offset
            script.removeAt(0)
        }

        mediator.load(LoadType.REFRESH, pagingState())
        val failed = mediator.load(LoadType.APPEND, pagingState())
        val retried = mediator.load(LoadType.APPEND, pagingState())

        assertTrue(failed is RemoteMediator.MediatorResult.Error)
        assertTrue((retried as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
        assertEquals(listOf(0, 2, 2), offsets)
        assertEquals(listOf(1L, 2L, 3L), store.tracks("tracks:popular").map { it.id.value })
    }

    @Test
    fun refreshReplacesTheStoredPage() = runTest {
        val store = MemoryTrackPageStore()
        val script = mutableListOf(
            DomainResult.Success(listOf(track(1), track(2))),
            DomainResult.Success(listOf(track(4))),
        )
        val mediator = mediator(store) { script.removeAt(0) }

        mediator.load(LoadType.REFRESH, pagingState())
        val refreshed = mediator.load(LoadType.REFRESH, pagingState())

        assertTrue((refreshed as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
        assertEquals(listOf(4L), store.tracks("tracks:popular").map { it.id.value })
    }

    @Test
    fun aRepeatedTrackIsStoredOnce() = runTest {
        val store = MemoryTrackPageStore()
        val script = mutableListOf(
            DomainResult.Success(listOf(track(1), track(2))),
            DomainResult.Success(listOf(track(2), track(3))),
        )
        val mediator = mediator(store) { script.removeAt(0) }

        mediator.load(LoadType.REFRESH, pagingState())
        mediator.load(LoadType.APPEND, pagingState())

        assertEquals(listOf(1L, 2L, 3L), store.tracks("tracks:popular").map { it.id.value })
    }

    @Test
    fun aFreshPageSkipsTheNetworkUntilRefresh() = runTest {
        val store = MemoryTrackPageStore()
        store.write(page(listOf(track(1)), nextOffset = null, observedAt = now))
        var calls = 0
        val mediator = mediator(store) {
            calls += 1
            DomainResult.Success(listOf(track(2)))
        }

        assertEquals(RemoteMediator.InitializeAction.SKIP_INITIAL_REFRESH, mediator.initialize())
        assertEquals(0, calls)
        mediator.load(LoadType.REFRESH, pagingState())
        assertEquals(1, calls)
        assertEquals(listOf(2L), store.tracks("tracks:popular").map { it.id.value })
    }

    @Test
    fun blankSearchDoesNotRequestTracks() = runTest {
        val remote = mockk<JamendoTrackDataSource>()
        val paging = TrackPagingRepositoryImpl(remote, MemoryTrackPageStore())

        paging.searchTracks("   ").collect { }

        coVerify(exactly = 0) { remote.searchTracks(any(), any()) }
    }

    @Test
    fun popularPagingRequestsTheFirstOffset() = runTest {
        val remote = mockk<JamendoTrackDataSource>()
        coEvery { remote.getPopularTracks(0) } returns NetworkResult.Success(listOf(trackDto(1)))
        val store = MemoryTrackPageStore()
        val paging = TrackPagingRepositoryImpl(remote, store) { now }

        val presenter = RecordingPresenter<Track>(coroutineContext)
        val job = launch { paging.popularTracks().collectLatest { presenter.collectFrom(it) } }
        presenter.loadStateFlow.first { it?.refresh is LoadState.NotLoading }

        coVerify(exactly = 1) { remote.getPopularTracks(0) }
        assertEquals(listOf(1L), store.tracks("tracks:popular").map { it.id.value })
        job.cancel()
    }

    @Test
    fun loadStatesMoveThroughLoadingErrorAndNotLoading() = runTest {
        val gate = FetchGate()
        val store = MemoryTrackPageStore()
        val flow = trackPager(
            catalogKey = "tracks:popular",
            maxAgeMillis = CATALOG_MAX_AGE_MILLIS,
            fetch = { offset -> gate.fetch(offset) },
            store = store,
            clock = { now },
            pageSize = 2,
        )
        val presenter = RecordingPresenter<Track>(coroutineContext)
        val job = launch { flow.collectLatest { presenter.collectFrom(it) } }

        val first = gate.next()
        runCurrent()
        val loading = presenter.loadStateFlow.value
        assertEquals(LoadState.Loading, loading?.refresh)
        first.complete(DomainResult.Failure(DomainError.Offline))
        val failed = presenter.loadStateFlow.first { it?.refresh is LoadState.Error }
        val failedRefresh = failed?.refresh as LoadState.Error
        assertEquals(DomainError.Offline, (failedRefresh.error as PageLoadException).error)

        presenter.retry()
        val retried = gate.next()
        runCurrent()
        assertEquals(LoadState.Loading, presenter.loadStateFlow.value?.refresh)
        retried.complete(DomainResult.Success(listOf(track(1))))
        val done = presenter.loadStateFlow.first { it?.refresh is LoadState.NotLoading }
        assertTrue(done?.refresh is LoadState.NotLoading)
        assertTrue((done?.append as LoadState.NotLoading).endOfPaginationReached)
        assertEquals(listOf(1L), store.tracks("tracks:popular").map { it.id.value })
        job.cancel()
    }

    private fun mediator(
        store: MemoryTrackPageStore,
        fetch: suspend (Int) -> DomainResult<List<Track>>,
    ) = TrackRemoteMediator(
        catalogKey = "tracks:popular",
        pageSize = 2,
        maxAgeMillis = CATALOG_MAX_AGE_MILLIS,
        fetch = fetch,
        store = store,
        clock = { now },
    )

    private fun page(
        tracks: List<Track>,
        nextOffset: Int?,
        observedAt: Instant,
    ) = PageWrite(
        catalogKey = "tracks:popular",
        offset = 0,
        tracks = tracks,
        replace = true,
        nextOffset = nextOffset,
        observedAt = observedAt,
    )
}

private class FetchGate {
    private val requests = Channel<CompletableDeferred<DomainResult<List<Track>>>>(Channel.UNLIMITED)

    suspend fun fetch(@Suppress("UNUSED_PARAMETER") offset: Int): DomainResult<List<Track>> {
        val response = CompletableDeferred<DomainResult<List<Track>>>()
        requests.send(response)
        return response.await()
    }

    suspend fun next(): CompletableDeferred<DomainResult<List<Track>>> = requests.receive()
}

private class RecordingPresenter<T : Any>(
    mainContext: kotlin.coroutines.CoroutineContext,
) : PagingDataPresenter<T>(mainContext) {
    override suspend fun presentPagingDataEvent(event: androidx.paging.PagingDataEvent<T>) = Unit
}

private fun pagingState() = PagingState<Int, Track>(
    pages = emptyList(),
    anchorPosition = null,
    config = PagingConfig(pageSize = 2),
    leadingPlaceholderCount = 0,
)

private class MemoryTrackPageStore : TrackPageStore {
    private val pages = mutableMapOf<String, Stored>()

    fun tracks(catalogKey: String): List<Track> =
        pages[catalogKey]?.placed.orEmpty().sortedBy { it.position }.map { it.track }

    override suspend fun header(catalogKey: String): CatalogPageHeader? =
        pages[catalogKey]?.let { CatalogPageHeader(it.fetchedAt, it.nextOffset) }

    override suspend fun write(request: PageWrite) {
        val current = pages[request.catalogKey]
        val existing = if (request.replace) {
            emptySet()
        } else {
            current?.placed.orEmpty().map { it.track.id.value }.toSet()
        }
        val placed = placeTracks(request.offset, request.tracks, existing)
        val kept = if (request.replace) mutableListOf() else current?.placed.orEmpty().toMutableList()
        kept += placed
        pages[request.catalogKey] = Stored(
            fetchedAt = Instant.ofEpochMilli(
                pageFetchedAt(
                    replace = request.replace,
                    previous = current?.fetchedAt?.toEpochMilli(),
                    observedAt = request.observedAt.toEpochMilli(),
                ),
            ),
            nextOffset = request.nextOffset,
            placed = kept,
        )
    }

    override fun pagingSource(catalogKey: String): PagingSource<Int, Track> =
        object : PagingSource<Int, Track>() {
            override fun getRefreshKey(state: PagingState<Int, Track>): Int? = null

            override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Track> {
                val items = tracks(catalogKey)
                val start = params.key ?: 0
                val end = minOf(start + params.loadSize, items.size)
                val slice = if (start >= items.size) emptyList() else items.subList(start, end)
                return LoadResult.Page(
                    data = slice,
                    prevKey = if (start == 0) null else start,
                    nextKey = if (end >= items.size) null else end,
                )
            }
        }

    private data class Stored(
        val fetchedAt: Instant,
        val nextOffset: Int?,
        val placed: List<PlacedTrack>,
    )
}

private fun track(id: Long) = Track(
    id = TrackId(id),
    title = "Faixa $id",
    artist = ArtistRef(ArtistId(7), "Luna"),
    album = AlbumRef(AlbumId(3), "Noite"),
    artworkUrl = null,
    durationMs = 1_000,
    streamUrl = null,
    licenseUrl = "https://creativecommons.org/licenses/by/4.0/",
    source = ContentSource("Jamendo", "https://www.jamendo.com/track/$id"),
    releaseDate = null,
)

private fun trackDto(id: Long) = JamendoTrackDto(
    id = id.toString(),
    name = "Faixa $id",
    duration = "1",
    artistId = "7",
    artistName = "Luna",
    albumId = "3",
    albumName = "Noite",
    licenseUrl = "https://creativecommons.org/licenses/by/4.0/",
    shareUrl = "https://www.jamendo.com/track/$id",
    audio = "https://example.com/stream/$id",
)
