@file:OptIn(androidx.paging.ExperimentalPagingApi::class)

package br.com.musicflow.data.paging

import androidx.paging.LoadType
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import br.com.musicflow.common.domain.DomainResult
import br.com.musicflow.common.domain.PageLoadException
import br.com.musicflow.common.domain.Track
import br.com.musicflow.data.local.CATALOG_PAGE_SIZE
import br.com.musicflow.data.local.isFresh
import kotlinx.coroutines.flow.Flow
import java.time.Instant

internal class TrackRemoteMediator(
    private val catalogKey: String,
    private val pageSize: Int,
    private val maxAgeMillis: Long,
    private val fetch: suspend (offset: Int) -> DomainResult<List<Track>>,
    private val store: TrackPageStore,
    private val clock: () -> Instant,
) : RemoteMediator<Int, Track>() {
    override suspend fun initialize(): InitializeAction {
        val header = store.header(catalogKey) ?: return InitializeAction.LAUNCH_INITIAL_REFRESH
        val fresh = isFresh(header.fetchedAt.toEpochMilli(), clock().toEpochMilli(), maxAgeMillis)
        return if (fresh) InitializeAction.SKIP_INITIAL_REFRESH else InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    override suspend fun load(loadType: LoadType, state: PagingState<Int, Track>): MediatorResult {
        val offset = when (loadType) {
            LoadType.REFRESH -> 0
            // Jamendo only moves forward with offset.
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.APPEND -> store.header(catalogKey)?.nextOffset
                ?: return MediatorResult.Success(endOfPaginationReached = true)
        }
        return when (val result = fetch(offset)) {
            is DomainResult.Failure -> MediatorResult.Error(PageLoadException(result.error))
            is DomainResult.Success -> {
                val reachedEnd = result.value.size < pageSize
                store.write(
                    PageWrite(
                        catalogKey = catalogKey,
                        offset = offset,
                        tracks = result.value,
                        replace = loadType == LoadType.REFRESH,
                        nextOffset = if (reachedEnd) null else offset + result.value.size,
                        observedAt = clock(),
                    ),
                )
                MediatorResult.Success(endOfPaginationReached = reachedEnd)
            }
        }
    }
}

internal fun trackPager(
    catalogKey: String,
    maxAgeMillis: Long,
    fetch: suspend (offset: Int) -> DomainResult<List<Track>>,
    store: TrackPageStore,
    clock: () -> Instant = Instant::now,
    pageSize: Int = CATALOG_PAGE_SIZE,
): Flow<PagingData<Track>> = Pager(
    config = PagingConfig(
        pageSize = pageSize,
        // One Jamendo page per request. The default initial size would ask for three pages at offset zero.
        initialLoadSize = pageSize,
        prefetchDistance = pageSize,
        enablePlaceholders = false,
    ),
    remoteMediator = TrackRemoteMediator(
        catalogKey = catalogKey,
        pageSize = pageSize,
        maxAgeMillis = maxAgeMillis,
        fetch = fetch,
        store = store,
        clock = clock,
    ),
    pagingSourceFactory = { store.pagingSource(catalogKey) },
).flow
