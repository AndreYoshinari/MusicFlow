package br.com.musicflow.player

internal fun isPlayableStream(url: String): Boolean {
    if (url.isBlank() || url != url.trim()) return false
    val separator = "://"
    val schemeEnd = url.indexOf(separator)
    if (schemeEnd <= 0) return false
    val scheme = url.substring(0, schemeEnd)
    if (scheme != "https" && scheme != "http") return false
    val remainder = url.substring(schemeEnd + separator.length)
    if (remainder.isBlank()) return false
    val host = remainder.substringBefore('/').substringBefore('?').substringBefore('#')
    return host.isNotBlank() && ' ' !in host
}
