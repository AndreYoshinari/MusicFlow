package br.com.musicflow.common.domain

data class ContentSource(
    val provider: String,
    val pageUrl: String,
) {
    init {
        require(provider.isNotBlank() && provider == provider.trim()) {
            "Content provider must not be blank."
        }
        require(pageUrl.isNotBlank() && pageUrl == pageUrl.trim()) {
            "Content page URL must not be blank."
        }
    }
}
