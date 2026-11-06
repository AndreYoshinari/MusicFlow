package br.com.musicflow.player

import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata

internal fun mediaItemFor(media: PlayableMedia): MediaItem {
    val metadata = MediaMetadata.Builder()
        .setTitle(media.title)
        .setArtist(media.artist)
        .setArtworkUri(artworkUrlOrNull(media.artworkUrl)?.let(Uri::parse))
        .build()
    return MediaItem.Builder()
        .setMediaId(media.mediaId)
        .setUri(media.uri)
        .setMediaMetadata(metadata)
        .build()
}
