package br.com.musicflow.feature.search

import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.LoadState
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import br.com.musicflow.common.domain.Album
import br.com.musicflow.common.domain.AlbumId
import br.com.musicflow.common.domain.AlbumRef
import br.com.musicflow.common.domain.Artist
import br.com.musicflow.common.domain.ArtistId
import br.com.musicflow.common.domain.ArtistRef
import br.com.musicflow.common.domain.ContentSource
import br.com.musicflow.common.domain.DomainError
import br.com.musicflow.common.domain.DomainResult
import br.com.musicflow.common.domain.PageLoadException
import br.com.musicflow.common.domain.Track
import br.com.musicflow.common.domain.TrackId
import br.com.musicflow.common.domain.repository.ArtistRepository
import br.com.musicflow.common.domain.repository.TrackPagingRepository
import br.com.musicflow.common.domain.usecase.ObserveTrackSearchUseCase
import br.com.musicflow.common.domain.usecase.SearchArtistsUseCase
import br.com.musicflow.common.domain.usecase.TRACK_SEARCH_DEBOUNCE_MILLIS
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {
    @Test
    fun validSearchReturnsTracksAndArtists() = runSearchTest {
        val paging = PagePaging(listOf(track(1, "Aurora")))
        val artists = ArtistFake().apply {
            result = DomainResult.Success(listOf(artist()))
        }
        val viewModel = start(paging, artists, collectTracks = false, debounceMillis = 0)

        viewModel.onQueryChange("  aurora  ")
        advanceTimeBy(TRACK_SEARCH_DEBOUNCE_MILLIS)
        runCurrent()

        val titles = loadedTitles(viewModel)
        assertEquals(listOf("aurora"), paging.searches)
        assertEquals(listOf("Aurora"), titles)
        assertEquals("  aurora  ", viewModel.state.value.query)
        val matches = viewModel.state.value.artists as ArtistMatches.Ready
        assertEquals(listOf("Estúdio Norte"), matches.artists.map { it.name })
    }

    @Test
    fun blankSearchDoesNotCallTheRepositories() = runSearchTest {
        val paging = RecordingPaging()
        val artists = ArtistFake()
        val viewModel = start(paging, artists)

        viewModel.onQueryChange("   ")
        advanceTimeBy(TRACK_SEARCH_DEBOUNCE_MILLIS)
        runCurrent()

        assertTrue(paging.searches.isEmpty())
        assertTrue(artists.calls.isEmpty())
        assertEquals(ArtistMatches.Hidden, viewModel.state.value.artists)
        assertEquals("   ", viewModel.state.value.query)
    }

    @Test
    fun debounceKeepsOnlyTheLatestTerm() = runSearchTest {
        val paging = RecordingPaging()
        val viewModel = start(paging, ArtistFake())

        viewModel.onQueryChange("au")
        viewModel.onQueryChange("aurora")
        advanceTimeBy(TRACK_SEARCH_DEBOUNCE_MILLIS - 1)
        runCurrent()
        assertTrue(paging.searches.isEmpty())

        advanceTimeBy(1)
        runCurrent()
        assertEquals(listOf("aurora"), paging.searches)
    }

    @Test
    fun aNewTermCancelsThePreviousSearch() = runSearchTest {
        val paging = RecordingPaging()
        val artists = ArtistFake().apply { hang = true }
        val viewModel = start(paging, artists)

        viewModel.onQueryChange("aurora")
        advanceTimeBy(TRACK_SEARCH_DEBOUNCE_MILLIS)
        runCurrent()
        viewModel.onQueryChange("noite")
        runCurrent()

        assertEquals(listOf("aurora"), paging.searches)
        assertEquals(listOf("aurora"), paging.cancelled)
        assertEquals(listOf("aurora"), artists.cancelled)

        advanceTimeBy(TRACK_SEARCH_DEBOUNCE_MILLIS)
        runCurrent()
        assertEquals(listOf("aurora", "noite"), paging.searches)
    }

    @Test
    fun clearCancelsTheActiveSearch() = runSearchTest {
        val paging = RecordingPaging()
        val viewModel = start(paging, ArtistFake())

        viewModel.onQueryChange("aurora")
        advanceTimeBy(TRACK_SEARCH_DEBOUNCE_MILLIS)
        runCurrent()
        viewModel.clear()
        runCurrent()

        assertEquals("", viewModel.state.value.query)
        assertEquals(ArtistMatches.Hidden, viewModel.state.value.artists)
        assertEquals(listOf("aurora"), paging.cancelled)
    }

    @Test
    fun artistFailureStaysInTheArtistSection() = runSearchTest {
        val artists = ArtistFake().apply { result = DomainResult.Failure(DomainError.Unavailable) }
        val viewModel = start(RecordingPaging(), artists)

        viewModel.onQueryChange("aurora")
        advanceTimeBy(TRACK_SEARCH_DEBOUNCE_MILLIS)
        runCurrent()

        assertEquals(ArtistMatches.Failed(DomainError.Unavailable), viewModel.state.value.artists)

        artists.result = DomainResult.Success(listOf(artist()))
        viewModel.retryArtists()
        runCurrent()
        val matches = viewModel.state.value.artists as ArtistMatches.Ready
        assertEquals("Estúdio Norte", matches.artists.single().name)
    }

    @Test
    fun trackFailureSurfacesTheDomainError() = runSearchTest {
        val paging = PagePaging(emptyList(), failure = DomainError.Offline)
        val viewModel = start(paging, ArtistFake(), collectTracks = false, debounceMillis = 0)

        viewModel.onQueryChange("aurora")
        val differ = trackDiffer()
        val refreshes = mutableListOf<LoadState>()
        val states = launch { differ.loadStateFlow.collect { refreshes += it.refresh } }
        val job = launch { viewModel.tracks.collectLatest { differ.submitData(it) } }
        advanceUntilIdle()
        job.cancel()
        states.cancel()

        val refresh = refreshes.last() as LoadState.Error
        val error = refresh.error as PageLoadException
        assertEquals(DomainError.Offline, error.error)
    }

    @Test
    fun paginationLoadsTheNextPage() = runSearchTest {
        val tracks = List(5) { index -> track(index + 1L, "Faixa ${index + 1}") }
        val paging = PagePaging(tracks)
        val viewModel = start(paging, ArtistFake(), collectTracks = false, debounceMillis = 0)

        viewModel.onQueryChange("faixa")
        val differ = trackDiffer()
        val job = launch { viewModel.tracks.collectLatest { differ.submitData(it) } }
        advanceUntilIdle()
        repeat(tracks.size) {
            if (differ.itemCount >= tracks.size) return@repeat
            val lastLoaded = differ.itemCount - 1
            if (lastLoaded < 0) return@repeat
            differ.getItem(lastLoaded)
            advanceUntilIdle()
        }
        job.cancel()

        val loaded = List(differ.itemCount) { index -> differ.peek(index)?.title }
        assertEquals(tracks.map { it.title }, loaded)
        assertTrue(paging.loads >= 2)
    }

    @Test
    fun emptyResultsStayEmpty() = runSearchTest {
        val paging = PagePaging(emptyList())
        val viewModel = start(paging, ArtistFake(), collectTracks = false, debounceMillis = 0)

        viewModel.onQueryChange("nada")
        advanceTimeBy(TRACK_SEARCH_DEBOUNCE_MILLIS)
        runCurrent()

        assertTrue(loadedTitles(viewModel).isEmpty())
        assertEquals(listOf("nada"), paging.searches)
        assertEquals(ArtistMatches.Hidden, viewModel.state.value.artists)
    }

    private fun TestScope.start(
        paging: TrackPagingRepository,
        artists: ArtistFake,
        collectTracks: Boolean = true,
        debounceMillis: Long = TRACK_SEARCH_DEBOUNCE_MILLIS,
    ): SearchViewModel {
        val viewModel = SearchViewModel(
            observeTrackSearch = ObserveTrackSearchUseCase(paging, debounceMillis),
            searchArtists = SearchArtistsUseCase(artists),
        )
        if (collectTracks) {
            backgroundScope.launch { viewModel.tracks.collect { } }
            runCurrent()
        }
        return viewModel
    }

    private fun TestScope.loadedTitles(viewModel: SearchViewModel): List<String> {
        val differ = trackDiffer()
        val job = launch { viewModel.tracks.collectLatest { differ.submitData(it) } }
        advanceUntilIdle()
        val titles = List(differ.itemCount) { index -> differ.peek(index)?.title }
        job.cancel()
        return titles.filterNotNull()
    }

    private fun TestScope.trackDiffer(): AsyncPagingDataDiffer<Track> {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        return AsyncPagingDataDiffer(
            diffCallback = object : DiffUtil.ItemCallback<Track>() {
                override fun areItemsTheSame(oldItem: Track, newItem: Track) = oldItem.id == newItem.id
                override fun areContentsTheSame(oldItem: Track, newItem: Track) = oldItem == newItem
            },
            updateCallback = object : ListUpdateCallback {
                override fun onInserted(position: Int, count: Int) = Unit
                override fun onRemoved(position: Int, count: Int) = Unit
                override fun onMoved(fromPosition: Int, toPosition: Int) = Unit
                override fun onChanged(position: Int, count: Int, payload: Any?) = Unit
            },
            mainDispatcher = dispatcher,
            workerDispatcher = dispatcher,
        )
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
private fun runSearchTest(block: suspend TestScope.() -> Unit) = runTest(timeout = 15.seconds) {
    Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
    try {
        block()
    } finally {
        Dispatchers.resetMain()
    }
}

private class RecordingPaging : TrackPagingRepository {
    val searches = mutableListOf<String>()
    val cancelled = mutableListOf<String>()

    override fun popularTracks(): Flow<PagingData<Track>> = emptyFlow()

    override fun tracksByArtist(artistId: ArtistId): Flow<PagingData<Track>> = emptyFlow()

    override fun searchTracks(query: String): Flow<PagingData<Track>> {
        searches += query
        return callbackFlow {
            trySend(PagingData.empty())
            awaitClose { cancelled += query }
        }
    }
}

private class PagePaging(
    private val pages: List<Track>,
    private val failure: DomainError? = null,
) : TrackPagingRepository {
    val searches = mutableListOf<String>()
    var loads = 0

    override fun popularTracks(): Flow<PagingData<Track>> = emptyFlow()

    override fun tracksByArtist(artistId: ArtistId): Flow<PagingData<Track>> = emptyFlow()

    override fun searchTracks(query: String): Flow<PagingData<Track>> {
        searches += query
        val error = failure
        return Pager(
            config = PagingConfig(
                pageSize = 2,
                initialLoadSize = 2,
                prefetchDistance = 1,
                enablePlaceholders = false,
            ),
        ) {
            object : PagingSource<Int, Track>() {
                override fun getRefreshKey(state: PagingState<Int, Track>): Int? = null

                override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Track> {
                    loads += 1
                    if (error != null) return LoadResult.Error(PageLoadException(error))
                    val start = params.key ?: 0
                    val data = pages.drop(start).take(params.loadSize)
                    val next = start + data.size
                    return LoadResult.Page(
                        data = data,
                        prevKey = null,
                        nextKey = if (next >= pages.size) null else next,
                    )
                }
            }
        }.flow
    }
}

private class ArtistFake : ArtistRepository {
    val calls = mutableListOf<String>()
    val cancelled = mutableListOf<String>()
    var result: DomainResult<List<Artist>> = DomainResult.Success(emptyList())
    var hang = false

    override suspend fun searchArtists(query: String): DomainResult<List<Artist>> {
        calls += query
        if (hang) {
            suspendCancellableCoroutine<Nothing> { continuation ->
                continuation.invokeOnCancellation { cancelled += query }
            }
        }
        return result
    }

    override suspend fun getArtistById(id: ArtistId): DomainResult<Artist> =
        DomainResult.Failure(DomainError.NotFound)

    override suspend fun getFeaturedArtists(): DomainResult<List<Artist>> = DomainResult.Success(emptyList())

    override suspend fun getAlbumsByArtist(artistId: ArtistId): DomainResult<List<Album>> =
        DomainResult.Success(emptyList())

    override suspend fun getPopularAlbums(): DomainResult<List<Album>> = DomainResult.Success(emptyList())
}

private fun artist() = Artist(
    id = ArtistId(7),
    name = "Estúdio Norte",
    imageUrl = "https://example.com/artist.jpg",
    source = ContentSource("Jamendo", "https://www.jamendo.com/artist/7"),
)

private fun track(id: Long, title: String) = Track(
    id = TrackId(id),
    title = title,
    artist = ArtistRef(ArtistId(7), "Luna Park"),
    album = AlbumRef(AlbumId(3), "Noite"),
    artworkUrl = "https://example.com/art/$id.jpg",
    durationMs = 180_000,
    streamUrl = null,
    licenseUrl = "https://creativecommons.org/licenses/by/4.0/",
    source = ContentSource("Jamendo", "https://www.jamendo.com/track/$id"),
    releaseDate = null,
)
