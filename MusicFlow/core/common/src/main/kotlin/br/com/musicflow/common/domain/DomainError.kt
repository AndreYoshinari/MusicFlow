package br.com.musicflow.common.domain

sealed interface DomainError {
    data object NotFound : DomainError

    data object Offline : DomainError

    data object Timeout : DomainError

    data object Unauthorized : DomainError

    data object Forbidden : DomainError

    data object RateLimited : DomainError

    data object Unavailable : DomainError

    data object InvalidResponse : DomainError

    data object InvalidName : DomainError

    data object InvalidArgument : DomainError

    data object DuplicateTrack : DomainError

    data object StreamUnavailable : DomainError
}
