package br.com.musicflow.network

import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class JamendoClientTest {
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
    fun popularTracksSendTheDocumentedQuery() = runTest {
        server.enqueue(successTrack())
        val result = client().getTracks(limit = 20, order = "popularity_total")

        val request = server.takeRequest()
        val url = requireNotNull(request.requestUrl)
        assertEquals("/tracks", url.encodedPath)
        assertEquals("test-client", url.queryParameter("client_id"))
        assertEquals("json", url.queryParameter("format"))
        assertEquals("20", url.queryParameter("limit"))
        assertEquals("popularity_total", url.queryParameter("order"))
        assertEquals("300", url.queryParameter("imagesize"))
        assertEquals("mp32", url.queryParameter("audioformat"))
        assertNull(url.queryParameter("fullcount"))
        assertNull(url.queryParameter("offset"))
        assertNull(url.queryParameter("search"))
        assertEquals("application/json", request.getHeader("Accept"))
        assertEquals("1848357", (result as NetworkResult.Success).value.single().id)
    }

    @Test
    fun trackPagesSendOffset() = runTest {
        server.enqueue(successTrack())
        client().getTracks(limit = 20, offset = 40, order = "popularity_total")

        val url = requireNotNull(server.takeRequest().requestUrl)
        assertEquals("20", url.queryParameter("limit"))
        assertEquals("40", url.queryParameter("offset"))
        assertNull(url.queryParameter("fullcount"))
    }

    @Test
    fun searchDoesNotOverrideRelevance() = runTest {
        server.enqueue(successTrack())
        client().getTracks(limit = 20, search = "aurora")

        val url = requireNotNull(server.takeRequest().requestUrl)
        assertEquals("aurora", url.queryParameter("search"))
        assertNull(url.queryParameter("order"))
    }

    @Test
    fun artistSearchUsesNamesearch() = runTest {
        server.enqueue(successArtist())
        val result = client().getArtists(limit = 20, nameSearch = "fankel")

        val url = requireNotNull(server.takeRequest().requestUrl)
        assertEquals("/artists", url.encodedPath)
        assertEquals("fankel", url.queryParameter("namesearch"))
        val artist = (result as NetworkResult.Success).value.single()
        assertEquals("376782", artist.id)
        assertEquals("WE ARE FM", artist.name)
    }

    @Test
    fun featuredArtistsAskForPopularity() = runTest {
        server.enqueue(successArtist())
        client().getArtists(limit = 20, order = "popularity_total")

        val url = requireNotNull(server.takeRequest().requestUrl)
        assertEquals("/artists", url.encodedPath)
        assertEquals("popularity_total", url.queryParameter("order"))
        assertEquals("20", url.queryParameter("limit"))
    }

    @Test
    fun albumsUseTheArtistAndCoverParameters() = runTest {
        server.enqueue(successAlbum())
        client().getAlbums(limit = 20, order = "releasedate_desc", artistId = "376782")

        val url = requireNotNull(server.takeRequest().requestUrl)
        assertEquals("/albums", url.encodedPath)
        assertEquals("376782", url.queryParameter("artist_id"))
        assertEquals("releasedate_desc", url.queryParameter("order"))
        assertEquals("300", url.queryParameter("imagesize"))
    }

    @Test
    fun numericIdsAreReadAsText() = runTest {
        server.enqueue(successTrack(id = "1848357", idQuoted = false))
        val result = client().getTracks(id = "1848357")

        assertEquals("1848357", (result as NetworkResult.Success).value.single().id)
        assertEquals("272", result.value.single().duration)
    }

    @Test
    fun emptyResultsStayEmpty() = runTest {
        server.enqueue(envelope(results = "[]"))
        val result = client().getTracks(search = "nada")

        assertEquals(NetworkResult.Success(emptyList<Any>()), result)
    }

    @Test
    fun apiFailureIsNotAnEmptyCatalog() = runTest {
        server.enqueue(
            envelope(
                code = 5,
                status = "failed",
                errorMessage = "Invalid Client Id",
                results = "[]",
            ),
        )

        val result = client().getTracks()

        assertEquals(NetworkResult.Failure(NetworkError.Api(5, "Invalid Client Id")), result)
    }

    @Test
    fun httpErrorsKeepTheirStatus() = runTest {
        listOf(400, 401, 403, 404, 429, 500).forEach { status ->
            server.enqueue(MockResponse().setResponseCode(status).setBody("""{"error":true}"""))
            val result = client().getTracks()
            assertEquals(NetworkResult.Failure(NetworkError.Http(status)), result)
        }
    }

    @Test
    fun invalidJsonIsNotAnEmptyCatalog() = runTest {
        server.enqueue(MockResponse().setBody("{"))

        val result = client().getTracks()

        assertEquals(NetworkResult.Failure(NetworkError.InvalidJson), result)
    }

    @Test
    fun timeoutIsNotAnEmptyCatalog() = runTest {
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE))

        val result = client(readTimeoutMillis = 200).getTracks()

        assertEquals(NetworkResult.Failure(NetworkError.Timeout), result)
    }

    @Test
    fun missingClientIdDoesNotCallTheApi() = runTest {
        val result = client(clientId = " ").getTracks()

        assertEquals(NetworkResult.Failure(NetworkError.MissingClientId), result)
        assertEquals(0, server.requestCount)
    }

    @Test
    fun connectionRefusedIsOffline() = runTest {
        val result = client(
            baseUrl = "http://127.0.0.1:1/",
            connectTimeoutMillis = 500,
        ).getTracks()

        assertEquals(NetworkResult.Failure(NetworkError.Offline), result)
    }

    private fun client(
        clientId: String = "test-client",
        baseUrl: String = server.url("/").toString(),
        connectTimeoutMillis: Long = 1_000,
        readTimeoutMillis: Long = 1_000,
    ): JamendoClient {
        val config = JamendoConfig(
            clientId = clientId,
            baseUrl = baseUrl,
            connectTimeoutMillis = connectTimeoutMillis,
            readTimeoutMillis = readTimeoutMillis,
            writeTimeoutMillis = 1_000,
        )
        return JamendoClient(createJamendoService(config), config)
    }
}

private fun successTrack(id: String = "1848357", idQuoted: Boolean = true): MockResponse =
    envelope(
        results = """
            [{
              "id": ${jsonScalar(id, idQuoted)},
              "name": "Aurora",
              "duration": 272,
              "artist_id": "421168",
              "artist_name": "fankel",
              "album_name": "Noite",
              "album_id": "368084",
              "license_ccurl": "http://creativecommons.org/licenses/by-nc-nd/3.0/",
              "releasedate": "2021-04-11",
              "album_image": "https://usercontent.jamendo.com?type=album&id=368084&width=300",
              "audio": "https://prod-1.storage.jamendo.com/?trackid=1848357&format=mp32",
              "image": "https://usercontent.jamendo.com?type=album&id=368084&width=300",
              "shareurl": "https://www.jamendo.com/track/1848357",
              "waveform": "ignored"
            }]
        """.trimIndent(),
    )

private fun successArtist(): MockResponse = envelope(
    results = """
        [{
          "id": "376782",
          "name": "WE ARE FM",
          "website": "https://www.facebook.com/wearefm",
          "joindate": "2011-12-29",
          "image": "https://usercontent.jamendo.com?type=artist&id=376782&width=300",
          "shorturl": "https://jamen.do/a/376782",
          "shareurl": "https://www.jamendo.com/artist/376782"
        }]
    """.trimIndent(),
)

private fun successAlbum(): MockResponse = envelope(
    results = """
        [{
          "id": "104336",
          "name": "Season One",
          "releasedate": "2011-12-29",
          "artist_id": "376782",
          "artist_name": "WE ARE FM",
          "image": "https://usercontent.jamendo.com?type=album&id=104336&width=300",
          "zip": "https://storage.jamendo.com/download/a104336/mp32/",
          "shareurl": "https://www.jamendo.com/list/a104336",
          "zip_allowed": true
        }]
    """.trimIndent(),
)

private fun envelope(
    code: Int = 0,
    status: String = "success",
    errorMessage: String = "",
    results: String,
): MockResponse = MockResponse().setBody(
    """
    {
      "headers": {
        "status": "$status",
        "code": $code,
        "error_message": "$errorMessage",
        "warnings": "",
        "results_count": 0
      },
      "results": $results
    }
    """.trimIndent(),
)

private fun jsonScalar(value: String, quoted: Boolean): String = if (quoted) "\"$value\"" else value
