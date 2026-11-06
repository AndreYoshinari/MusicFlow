package br.com.musicflow.common.domain

class PageLoadException(
    val error: DomainError,
) : Exception(error.toString())
