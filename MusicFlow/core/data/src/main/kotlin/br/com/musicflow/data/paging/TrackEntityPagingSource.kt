package br.com.musicflow.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import br.com.musicflow.common.domain.Track
import br.com.musicflow.data.local.toTrack
import br.com.musicflow.database.entity.CachedTrackEntity
import java.time.Instant

internal class TrackEntityPagingSource(
    private val origin: PagingSource<Int, CachedTrackEntity>,
    private val clock: () -> Instant,
) : PagingSource<Int, Track>() {
    init {
        origin.registerInvalidatedCallback(::invalidate)
        registerInvalidatedCallback {
            if (!origin.invalid) origin.invalidate()
        }
    }

    // A refresh replaces the Jamendo page from offset zero, so the local page restarts at the first row.
    override fun getRefreshKey(state: PagingState<Int, Track>): Int? = null

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Track> {
        val now = clock()
        return when (val result = origin.load(params)) {
            is LoadResult.Page -> LoadResult.Page(
                data = result.data.map { it.toTrack(now) },
                prevKey = result.prevKey,
                nextKey = result.nextKey,
                itemsBefore = result.itemsBefore,
                itemsAfter = result.itemsAfter,
            )
            is LoadResult.Error -> LoadResult.Error(result.throwable)
            is LoadResult.Invalid -> LoadResult.Invalid()
        }
    }
}
