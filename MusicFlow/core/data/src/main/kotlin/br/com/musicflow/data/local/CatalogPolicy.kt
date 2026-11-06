package br.com.musicflow.data.local

internal const val CATALOG_PAGE_SIZE = 20

internal const val CATALOG_MAX_AGE_MILLIS = 60 * 60 * 1_000L

internal const val SEARCH_MAX_AGE_MILLIS = 15 * 60 * 1_000L

internal sealed interface CatalogRead<out T> {
    data class Fresh<T>(val value: T) : CatalogRead<T>

    data class Stale<T>(val value: T) : CatalogRead<T>

    data object Missing : CatalogRead<Nothing>
}

internal fun ageMillis(observedAt: Long, now: Long): Long = now - observedAt

internal fun isFresh(observedAt: Long, now: Long, maxAgeMillis: Long): Boolean {
    val age = ageMillis(observedAt, now)
    return age < 0 || age <= maxAgeMillis
}
