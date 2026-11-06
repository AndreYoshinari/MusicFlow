package br.com.musicflow.network

import br.com.musicflow.network.api.JamendoApiService
import br.com.musicflow.network.dto.JamendoAlbumDto
import br.com.musicflow.network.dto.JamendoArtistDto
import br.com.musicflow.network.dto.JamendoResponse
import br.com.musicflow.network.dto.JamendoTrackDto
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.io.IOException
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

private val JSON_MEDIA_TYPE = "application/json".toMediaType()

fun createJamendoOkHttp(config: JamendoConfig): OkHttpClient = OkHttpClient.Builder()
    .connectTimeout(config.connectTimeoutMillis, TimeUnit.MILLISECONDS)
    .readTimeout(config.readTimeoutMillis, TimeUnit.MILLISECONDS)
    .writeTimeout(config.writeTimeoutMillis, TimeUnit.MILLISECONDS)
    .addInterceptor(AcceptJsonInterceptor())
    .addInterceptor(JamendoClientIdInterceptor(config.clientId))
    .build()

fun createJamendoRetrofit(
    config: JamendoConfig,
    client: OkHttpClient,
    json: Json = jamendoJson(),
): Retrofit = Retrofit.Builder()
    .baseUrl(config.baseUrl)
    .client(client)
    .addConverterFactory(json.asConverterFactory(JSON_MEDIA_TYPE))
    .build()

fun createJamendoService(
    config: JamendoConfig,
    json: Json = jamendoJson(),
): JamendoApiService = createJamendoRetrofit(
    config = config,
    client = createJamendoOkHttp(config),
    json = json,
).create(JamendoApiService::class.java)

internal fun jamendoJson(): Json = Json {
    ignoreUnknownKeys = true
}

class JamendoClient(
    private val service: JamendoApiService,
    private val config: JamendoConfig,
) {
    suspend fun getTracks(
        limit: Int? = null,
        offset: Int? = null,
        order: String? = null,
        id: String? = null,
        artistId: String? = null,
        search: String? = null,
    ): NetworkResult<List<JamendoTrackDto>> = execute {
        service.getTracks(
            limit = limit,
            offset = offset,
            order = order,
            id = id,
            artistId = artistId,
            search = search,
            imageSize = COVER_SIZE,
            audioFormat = STREAM_FORMAT,
        )
    }

    suspend fun getArtists(
        limit: Int? = null,
        order: String? = null,
        id: String? = null,
        nameSearch: String? = null,
    ): NetworkResult<List<JamendoArtistDto>> = execute {
        service.getArtists(limit = limit, order = order, id = id, nameSearch = nameSearch)
    }

    suspend fun getAlbums(
        limit: Int? = null,
        order: String? = null,
        artistId: String? = null,
    ): NetworkResult<List<JamendoAlbumDto>> = execute {
        service.getAlbums(
            limit = limit,
            order = order,
            artistId = artistId,
            imageSize = COVER_SIZE,
        )
    }

    private suspend fun <T> execute(
        block: suspend () -> JamendoResponse<T>,
    ): NetworkResult<List<T>> {
        if (config.clientId.isBlank()) {
            return NetworkResult.Failure(NetworkError.MissingClientId)
        }
        return try {
            val response = block()
            if (response.headers.code == SUCCESS_CODE) {
                NetworkResult.Success(response.results)
            } else {
                NetworkResult.Failure(
                    NetworkError.Api(
                        code = response.headers.code,
                        message = response.headers.errorMessage,
                    ),
                )
            }
        } catch (error: SerializationException) {
            NetworkResult.Failure(NetworkError.InvalidJson)
        } catch (error: SocketTimeoutException) {
            NetworkResult.Failure(NetworkError.Timeout)
        } catch (error: UnknownHostException) {
            NetworkResult.Failure(NetworkError.Offline)
        } catch (error: ConnectException) {
            NetworkResult.Failure(NetworkError.Offline)
        } catch (error: NoRouteToHostException) {
            NetworkResult.Failure(NetworkError.Offline)
        } catch (error: HttpException) {
            NetworkResult.Failure(NetworkError.Http(error.code()))
        } catch (error: IOException) {
            if (error.cause is SerializationException) {
                NetworkResult.Failure(NetworkError.InvalidJson)
            } else {
                NetworkResult.Failure(NetworkError.Unavailable)
            }
        }
    }

    private companion object {
        const val SUCCESS_CODE = 0
        const val COVER_SIZE = 300
        const val STREAM_FORMAT = "mp32"
    }
}
