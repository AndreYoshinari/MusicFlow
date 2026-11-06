package br.com.musicflow.data.repository

import br.com.musicflow.common.domain.DomainError
import br.com.musicflow.common.domain.DomainResult
import br.com.musicflow.data.local.CatalogRead
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap

internal class RequestGate {
    private val locks = ConcurrentHashMap<String, Mutex>()

    suspend fun <T> run(key: String, block: suspend () -> T): T =
        locks.getOrPut(key) { Mutex() }.withLock { block() }
}

internal suspend fun <T> syncCatalog(
    cached: CatalogRead<T>,
    remote: suspend () -> DomainResult<T>,
    save: suspend (T) -> Unit,
): DomainResult<T> {
    if (cached is CatalogRead.Fresh) return DomainResult.Success(cached.value)
    return when (val result = remote()) {
        is DomainResult.Success -> {
            save(result.value)
            result
        }
        is DomainResult.Failure -> if (result.error.keepsCachedCatalog() && cached is CatalogRead.Stale) {
            DomainResult.Success(cached.value)
        } else {
            result
        }
    }
}

private fun DomainError.keepsCachedCatalog(): Boolean = when (this) {
    DomainError.Offline,
    DomainError.Timeout,
    DomainError.Unavailable,
    DomainError.RateLimited,
    DomainError.Unauthorized,
    DomainError.Forbidden,
    DomainError.InvalidResponse,
    -> true
    else -> false
}
