package br.com.musicflow.player

import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.Player

internal class SessionPlayer(
    player: Player,
    private val playback: MusicPlayback,
) : ForwardingPlayer(player) {
    override fun getAvailableCommands(): Player.Commands {
        return super.getAvailableCommands()
            .buildUpon()
            .add(Player.COMMAND_PLAY_PAUSE)
            .add(Player.COMMAND_SEEK_TO_NEXT)
            .add(Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)
            .add(Player.COMMAND_SEEK_TO_PREVIOUS)
            .add(Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)
            .add(Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM)
            .build()
    }

    override fun seekToNext() = playback.next()

    override fun seekToNextMediaItem() = playback.next()

    override fun seekToPrevious() = playback.previous()

    override fun seekToPreviousMediaItem() = playback.previous()
}
