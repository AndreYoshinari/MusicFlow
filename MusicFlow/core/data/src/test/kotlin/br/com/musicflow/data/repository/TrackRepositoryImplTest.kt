package br.com.musicflow.data.repository

import br.com.musicflow.common.domain.ArtistId
import br.com.musicflow.common.domain.DomainError
import br.com.musicflow.common.domain.DomainResult
import br.com.musicflow.common.domain.TrackId
import br.com.musicflow.data.remote.JamendoTrackDataSource
import br.com.musicflow.network.NetworkError
import br.com.musicflow.network.NetworkResult
import br.com.musicflow.network.dto.JamendoTrackDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

class TrackRepositoryImplTest {
    private val remote = mockk<JamendoTrackDataSource>()
    private val local = MemoryTrackStore()
    private var now = Instant.parse("2026-11-05T18:00:00Z")
    private val repository = TrackRepositoryImpl(remote, local, RequestGate()) { now }

    @Test
    fun popularTracksComeFromTheNetworkAndAreStored() = runTest {
        coEvery { remote.getPopularTracks() } returns NetworkResult.Success(listOf(trackDto()))

        val result = repository.getPopularTracks()

        assertEquals("Aurora", (result as DomainResult.Success).value.single().title)
        coVerify(exactly = 1) { remote.getPopularTracks() }
    }

    @Test
    fun aFreshCatalogSkipsTheNetwork() = runTest {
        coEvery { remote.getPopularTracks() } returns NetworkResult.Success(listOf(trackDto()))
        repository.getPopularTracks()
        now = now.plusMillis(SEARCH_WINDOW)

        val result = repository.getPopularTracks()

        assertEquals(DomainResult.Success::class, result::class)
        coVerify(exactly = 1) { remote.getPopularTracks() }
    }

    @Test
    fun offlineWithoutCacheReturnsTheNetworkFailure() = runTest {
        coEvery { remote.getPopularTracks() } returns NetworkResult.Failure(NetworkError.Offline)

        val result = repository.getPopularTracks()

        assertEquals(DomainResult.Failure(DomainError.Offline), result)
    }

    @Test
    fun timeoutWithoutCacheIsNotAnEmptyCatalog() = runTest {
        coEvery { remote.searchTracks("aurora") } returns NetworkResult.Failure(NetworkError.Timeout)

        val result = repository.searchTracks("aurora")

        assertEquals(DomainResult.Failure(DomainError.Timeout), result)
    }

    @Test
    fun offlineWithAPreviousCatalogReturnsTheStoredTracks() = runTest {
        coEvery { remote.getPopularTracks() } returns NetworkResult.Success(listOf(trackDto()))
        repository.getPopularTracks()
        now = now.plusMillis(CATALOG_WINDOW)
        coEvery { remote.getPopularTracks() } returns NetworkResult.Failure(NetworkError.Offline)

        val result = repository.getPopularTracks()

        assertEquals("Aurora", (result as DomainResult.Success).value.single().title)
        coVerify(exactly = 2) { remote.getPopularTracks() }
    }

    @Test
    fun aStaleSearchIsRefreshedOnce() = runTest {
        coEvery { remote.searchTracks("aurora") } returns NetworkResult.Success(listOf(trackDto()))
        repository.searchTracks("aurora")
        now = now.plusMillis(SEARCH_WINDOW)
        coEvery { remote.searchTracks("aurora") } returns NetworkResult.Success(listOf(trackDto(name = "Nova")))

        val result = repository.searchTracks("aurora")

        assertEquals("Nova", (result as DomainResult.Success).value.single().title)
        coVerify(exactly = 2) { remote.searchTracks("aurora") }
    }

    @Test
    fun notFoundDoesNotRestoreAStaleTrack() = runTest {
        coEvery { remote.getTrack(TrackId(1)) } returns NetworkResult.Success(listOf(trackDto()))
        repository.getTrackById(TrackId(1))
        now = now.plusMillis(CATALOG_WINDOW)
        coEvery { remote.getTrack(TrackId(1)) } returns NetworkResult.Success(emptyList())

        val result = repository.getTrackById(TrackId(1))

        assertEquals(DomainResult.Failure(DomainError.NotFound), result)
    }

    @Test
    fun aFreshStreamUrlSkipsTheNetwork() = runTest {
        coEvery { remote.getTrack(TrackId(1)) } returns NetworkResult.Success(listOf(trackDto()))
        repository.getTrackStreamUrl(TrackId(1))

        val result = repository.getTrackStreamUrl(TrackId(1))

        assertEquals(DomainResult.Success("https://example.com/stream/1"), result)
        coVerify(exactly = 1) { remote.getTrack(TrackId(1)) }
    }

    @Test
    fun anExpiredStreamUrlIsRequestedAgainAndAFailureIsNotReplacedByTheOldUrl() = runTest {
        coEvery { remote.getTrack(TrackId(1)) } returns NetworkResult.Success(listOf(trackDto()))
        repository.getTrackStreamUrl(TrackId(1))
        now = now.plusMillis(SEARCH_WINDOW)
        coEvery { remote.getTrack(TrackId(1)) } returns NetworkResult.Failure(NetworkError.Offline)

        val result = repository.getTrackStreamUrl(TrackId(1))

        assertEquals(DomainResult.Failure(DomainError.Offline), result)
        val cached = repository.getTrackById(TrackId(1)) as DomainResult.Success
        assertNull(cached.value.streamUrl)
    }

    @Test
    fun aSuccessfulResponseWithoutAudioIsStreamUnavailable() = runTest {
        coEvery { remote.getTrack(TrackId(1)) } returns NetworkResult.Success(listOf(trackDto(audio = "")))

        val result = repository.getTrackStreamUrl(TrackId(1))

        assertEquals(DomainResult.Failure(DomainError.StreamUnavailable), result)
    }

    @Test
    fun tracksByArtistFollowTheSameCacheRule() = runTest {
        val artistId = ArtistId(7)
        coEvery { remote.getTracksByArtist(artistId) } returns NetworkResult.Success(emptyList())

        val first = repository.getTracksByArtist(artistId)
        val second = repository.getTracksByArtist(artistId)

        assertEquals(DomainResult.Success(emptyList<Nothing>()), first)
        assertEquals(first, second)
        coVerify(exactly = 1) { remote.getTracksByArtist(artistId) }
    }

    private companion object {
        const val SEARCH_WINDOW = 15 * 60 * 1_000L + 1
        const val CATALOG_WINDOW = 60 * 60 * 1_000L + 1
    }
}

private fun trackDto(
    name: String = "Aurora",
    audio: String = "https://example.com/stream/1",
) = JamendoTrackDto(
    id = "1",
    name = name,
    duration = "180",
    artistId = "7",
    artistName = "Luna",
    albumId = "3",
    albumName = "Noite",
    licenseUrl = "https://creativecommons.org/licenses/by/4.0/",
    shareUrl = "https://www.jamendo.com/track/1",
    audio = audio,
)
