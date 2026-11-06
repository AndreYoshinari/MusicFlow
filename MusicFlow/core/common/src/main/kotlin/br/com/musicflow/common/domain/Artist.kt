package br.com.musicflow.common.domain

data class ArtistRef(
    val id: ArtistId,
    val name: String,
) {
    init {
        require(name.isNotBlank() && name == name.trim()) { "Artist name must not be blank." }
    }
}

data class Artist(
    val id: ArtistId,
    val name: String,
    val imageUrl: String?,
    val source: ContentSource,
) {
    init {
        require(name.isNotBlank() && name == name.trim()) { "Artist name must not be blank." }
        require(imageUrl == null || imageUrl.isNotBlank()) { "Artist image URL must not be blank." }
    }

    fun toRef(): ArtistRef = ArtistRef(id, name)
}
