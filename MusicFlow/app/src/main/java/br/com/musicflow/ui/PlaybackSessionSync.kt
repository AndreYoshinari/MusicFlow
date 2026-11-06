package br.com.musicflow.ui

import br.com.musicflow.common.domain.Track
import br.com.musicflow.player.PlaybackTrack

internal fun Track.toPlaybackTrack(): PlaybackTrack = PlaybackTrack(
    id = id,
    title = title,
    artistName = artist.name,
    artworkUrl = artworkUrl,
    streamUrl = streamUrl.orEmpty(),
)
