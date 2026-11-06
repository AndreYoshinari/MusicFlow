package br.com.musicflow.data

import br.com.musicflow.common.domain.AlbumId
import br.com.musicflow.common.domain.ArtistId
import br.com.musicflow.common.domain.DomainError
import br.com.musicflow.common.domain.DomainResult
import br.com.musicflow.common.domain.TrackId
import br.com.musicflow.data.remote.JamendoArtistDataSource
import br.com.musicflow.data.remote.JamendoTrackDataSource
import br.com.musicflow.data.repository.ArtistRepositoryImpl
import br.com.musicflow.data.repository.MemoryArtistStore
import br.com.musicflow.data.repository.MemoryTrackStore
import br.com.musicflow.data.repository.RequestGate
import br.com.musicflow.data.repository.TrackRepositoryImpl
import br.com.musicflow.network.JamendoClient
import br.com.musicflow.network.JamendoConfig
import br.com.musicflow.network.createJamendoService
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class JamendoCatalogTest {
    private val server = MockWebServer()

    @Before
    fun setUp() {
        server.start()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun trackUsesLicenseCoverStreamAndDurationFromThePayload() = runTest {
        server.enqueue(trackResponse())

        val result = tracks().getTrackById(TrackId(1_848_357))

        val track = (result as DomainResult.Success).value
        assertEquals(TrackId(1_848_357), track.id)
        assertEquals("Aurora", track.title)
        assertEquals(ArtistId(421_168), track.artist.id)
        assertEquals(AlbumId(368_084), track.album.id)
        assertEquals(272_000L, track.durationMs)
        assertEquals("http://creativecommons.org/licenses/by-nc-nd/3.0/", track.licenseUrl)
        assertEquals("https://prod-1.storage.jamendo.com/?trackid=1848357&format=mp32", track.streamUrl)
        assertEquals("https://usercontent.jamendo.com?type=album&id=368084&width=300", track.artworkUrl)
        assertEquals("https://www.jamendo.com/track/1848357", track.source.pageUrl)
        assertEquals("Jamendo", track.source.provider)
        assertEquals(LocalDate.of(2021, 4, 11), track.releaseDate)
    }

    @Test
    fun blankAlbumCoverFallsBackToTheTrackImage() = runTest {
        server.enqueue(trackResponse(albumImage = "", image = "https://usercontent.jamendo.com/track.jpg"))

        val track = (tracks().getTrackById(TrackId(1_848_357)) as DomainResult.Success).value

        assertEquals("https://usercontent.jamendo.com/track.jpg", track.artworkUrl)
    }

    @Test
    fun searchWithoutResultsIsEmpty() = runTest {
        server.enqueue(trackResponse(results = "[]"))

        val result = tracks().searchTracks("nada")

        assertEquals(DomainResult.Success(emptyList<Nothing>()), result)
    }

    @Test
    fun missingTrackIsNotFound() = runTest {
        server.enqueue(trackResponse(results = "[]"))

        val result = tracks().getTrackById(TrackId(1_848_357))

        assertEquals(DomainResult.Failure(DomainError.NotFound), result)
    }

    @Test
    fun incompleteTrackIsNotReturnedAsEmpty() = runTest {
        server.enqueue(trackResponse(licenseUrl = ""))

        val result = tracks().getPopularTracks()

        assertEquals(DomainResult.Failure(DomainError.InvalidResponse), result)
    }

    @Test
    fun singleWithoutAnAlbumIsNotReturnedAsEmpty() = runTest {
        server.enqueue(trackResponse(albumId = "", albumName = ""))

        val result = tracks().getTracksByArtist(ArtistId(421_168))

        assertEquals(DomainResult.Failure(DomainError.InvalidResponse), result)
    }

    @Test
    fun missingStreamUrlStaysAFailure() = runTest {
        server.enqueue(trackResponse(audio = ""))
        server.enqueue(trackResponse(audio = ""))
        val repository = tracks()

        val details = repository.getTrackById(TrackId(1_848_357))
        val stream = repository.getTrackStreamUrl(TrackId(1_848_357))

        assertNull((details as DomainResult.Success).value.streamUrl)
        assertEquals(DomainResult.Failure(DomainError.StreamUnavailable), stream)
    }

    @Test
    fun timeoutIsNotAnEmptyCatalog() = runTest {
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE))

        val result = tracks(readTimeoutMillis = 200).getPopularTracks()

        assertEquals(DomainResult.Failure(DomainError.Timeout), result)
    }

    @Test
    fun invalidJsonIsNotAnEmptyCatalog() = runTest {
        server.enqueue(MockResponse().setBody("{"))

        val result = tracks().searchTracks("aurora")

        assertEquals(DomainResult.Failure(DomainError.InvalidResponse), result)
    }

    @Test
    fun httpStatusesReachTheDomain() = runTest {
        val expected = mapOf(
            400 to DomainError.InvalidArgument,
            401 to DomainError.Unauthorized,
            403 to DomainError.Forbidden,
            404 to DomainError.NotFound,
            429 to DomainError.RateLimited,
            500 to DomainError.Unavailable,
        )
        val repository = tracks()
        expected.forEach { (status, error) ->
            server.enqueue(MockResponse().setResponseCode(status).setBody("""{"error":true}"""))
            assertEquals(DomainResult.Failure(error), repository.getPopularTracks())
        }
    }

    @Test
    fun apiCodesReachTheDomain() = runTest {
        val expected = mapOf(
            5 to DomainError.Unauthorized,
            6 to DomainError.RateLimited,
            11 to DomainError.Forbidden,
        )
        val repository = tracks()
        expected.forEach { (code, error) ->
            server.enqueue(trackResponse(code = code, status = "failed", results = "[]"))
            assertEquals(DomainResult.Failure(error), repository.searchTracks("aurora"))
        }
    }

    @Test
    fun artistAndAlbumsKeepTheDocumentedFields() = runTest {
        server.enqueue(artistResponse())
        server.enqueue(albumResponse())
        val repository = artists()

        val artist = repository.getArtistById(ArtistId(376_782)) as DomainResult.Success
        val albums = repository.getAlbumsByArtist(ArtistId(376_782)) as DomainResult.Success

        assertEquals("WE ARE FM", artist.value.name)
        assertEquals("https://usercontent.jamendo.com?type=artist&id=376782&width=300", artist.value.imageUrl)
        assertEquals("https://www.jamendo.com/artist/376782", artist.value.source.pageUrl)
        assertEquals("Season One", albums.value.single().title)
        assertEquals(LocalDate.of(2011, 12, 29), albums.value.single().releaseDate)
        assertEquals("https://www.jamendo.com/list/a104336", albums.value.single().source.pageUrl)
    }

    @Test
    fun artistSearchCanBeEmpty() = runTest {
        server.enqueue(artistResponse(results = "[]"))

        val result = artists().searchArtists("ninguem")

        assertEquals(DomainResult.Success(emptyList<Nothing>()), result)
    }

    private fun tracks(readTimeoutMillis: Long = 1_000): TrackRepositoryImpl {
        val client = client(readTimeoutMillis)
        return TrackRepositoryImpl(
            JamendoTrackDataSource(client),
            MemoryTrackStore(),
            RequestGate(),
        )
    }

    private fun artists(): ArtistRepositoryImpl {
        return ArtistRepositoryImpl(
            JamendoArtistDataSource(client()),
            MemoryArtistStore(),
            RequestGate(),
        )
    }

    private fun client(readTimeoutMillis: Long = 1_000): JamendoClient {
        val config = JamendoConfig(
            clientId = "test-client",
            baseUrl = server.url("/").toString(),
            connectTimeoutMillis = 1_000,
            readTimeoutMillis = readTimeoutMillis,
            writeTimeoutMillis = 1_000,
        )
        return JamendoClient(createJamendoService(config), config)
    }
}

private fun trackResponse(
    code: Int = 0,
    status: String = "success",
    albumId: String = "368084",
    albumName: String = "Noite",
    albumImage: String = "https://usercontent.jamendo.com?type=album&id=368084&width=300",
    image: String = "https://usercontent.jamendo.com?type=album&id=368084&width=300",
    audio: String = "https://prod-1.storage.jamendo.com/?trackid=1848357&format=mp32",
    licenseUrl: String = "http://creativecommons.org/licenses/by-nc-nd/3.0/",
    results: String? = null,
): MockResponse {
    val body = results ?: """
        [{
          "id": "1848357",
          "name": "Aurora",
          "duration": 272,
          "artist_id": "421168",
          "artist_name": "fankel",
          "album_name": "$albumName",
          "album_id": "$albumId",
          "license_ccurl": "$licenseUrl",
          "releasedate": "2021-04-11",
          "album_image": "$albumImage",
          "audio": "$audio",
          "image": "$image",
          "shareurl": "https://www.jamendo.com/track/1848357"
        }]
    """.trimIndent()
    return envelope(code, status, body)
}

private fun artistResponse(results: String? = null): MockResponse = envelope(
    results = results ?: """
        [{
          "id": "376782",
          "name": "WE ARE FM",
          "image": "https://usercontent.jamendo.com?type=artist&id=376782&width=300",
          "shareurl": "https://www.jamendo.com/artist/376782"
        }]
    """.trimIndent(),
)

private fun albumResponse(): MockResponse = envelope(
    results = """
        [{
          "id": "104336",
          "name": "Season One",
          "releasedate": "2011-12-29",
          "artist_id": "376782",
          "artist_name": "WE ARE FM",
          "image": "https://usercontent.jamendo.com?type=album&id=104336&width=300",
          "shareurl": "https://www.jamendo.com/list/a104336"
        }]
    """.trimIndent(),
)

private fun envelope(code: Int = 0, status: String = "success", results: String): MockResponse =
    MockResponse().setBody(
        """
        {
          "headers": {
            "status": "$status",
            "code": $code,
            "error_message": "",
            "warnings": "",
            "results_count": 0
          },
          "results": $results
        }
        """.trimIndent(),
    )
