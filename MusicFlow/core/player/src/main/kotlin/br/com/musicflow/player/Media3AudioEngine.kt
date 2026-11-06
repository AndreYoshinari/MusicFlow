package br.com.musicflow.player

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

internal class Media3AudioEngine(
    context: Context,
) : AudioEngine {
    private val appContext = context.applicationContext
    private val handler = Handler(Looper.getMainLooper())
    private val connection = PlayerConnection<ExoPlayer>()
    private val ticker = Runnable { publishPosition(scheduleNext = true) }

    private var listener: AudioEngineListener? = null
    private var currentMedia: PlayableMedia? = null
    private var volume = 1f
    private var serviceRunning = false

    @Volatile
    private var released = false

    override fun play(media: PlayableMedia, positionMs: Long) {
        if (released) return
        currentMedia = media
        ensureService()
        onPlayer { exoPlayer ->
            exoPlayer.setMediaItem(mediaItemFor(media), positionMs.coerceAtLeast(0))
            exoPlayer.prepare()
            exoPlayer.play()
            scheduleTicker()
        }
    }

    override fun pause() {
        onPlayer { exoPlayer ->
            exoPlayer.pause()
            handler.removeCallbacks(ticker)
            publishPosition(scheduleNext = false)
        }
    }

    override fun resume() {
        val media = currentMedia ?: return
        if (released) return
        ensureService()
        onPlayer { exoPlayer ->
            if (exoPlayer.playbackState == Player.STATE_IDLE || exoPlayer.mediaItemCount == 0) {
                exoPlayer.setMediaItem(mediaItemFor(media), exoPlayer.currentPosition.coerceAtLeast(0))
                exoPlayer.prepare()
            }
            exoPlayer.play()
            scheduleTicker()
        }
    }

    override fun stop() {
        onPlayer { exoPlayer ->
            handler.removeCallbacks(ticker)
            exoPlayer.pause()
            exoPlayer.seekTo(0)
            exoPlayer.stop()
            exoPlayer.clearMediaItems()
        }
    }

    override fun seekTo(positionMs: Long) {
        onPlayer { exoPlayer ->
            exoPlayer.seekTo(positionMs.coerceAtLeast(0))
            publishPosition(scheduleNext = false)
        }
    }

    override fun setVolume(volume: Float) {
        this.volume = volume
        onPlayer { exoPlayer -> exoPlayer.volume = volume }
    }

    override fun addListener(listener: AudioEngineListener) {
        this.listener = listener
    }

    override fun removeListener(listener: AudioEngineListener) {
        if (this.listener === listener) this.listener = null
    }

    override fun release() {
        val releasePlayer = Runnable {
            if (released) return@Runnable
            released = true
            handler.removeCallbacks(ticker)
            connection.clearPending()
            connection.current()?.let { exoPlayer -> detach(exoPlayer) }
            listener = null
        }
        if (Looper.myLooper() == Looper.getMainLooper()) releasePlayer.run() else handler.post(releasePlayer)
    }

    fun attach(player: ExoPlayer) {
        if (released) {
            player.release()
            return
        }
        player.addListener(playerListener)
        player.volume = volume
        serviceRunning = true
        connection.attach(player)
    }

    fun detach(player: ExoPlayer) {
        handler.removeCallbacks(ticker)
        player.removeListener(playerListener)
        connection.detach(player)
        player.release()
        serviceRunning = false
    }

    private fun onPlayer(block: (ExoPlayer) -> Unit) {
        if (released) return
        val action = Runnable {
            if (!released) connection.whenReady(block)
        }
        if (Looper.myLooper() == Looper.getMainLooper()) action.run() else handler.post(action)
    }

    private fun ensureService() {
        if (serviceRunning) return
        val intent = Intent(appContext, PlaybackService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            appContext.startForegroundService(intent)
        } else {
            appContext.startService(intent)
        }
    }

    private fun scheduleTicker() {
        handler.removeCallbacks(ticker)
        handler.postDelayed(ticker, POSITION_INTERVAL_MS)
    }

    private fun publishPosition(scheduleNext: Boolean) {
        val exoPlayer = connection.current() ?: return
        if (released) return
        val duration = exoPlayer.duration
        listener?.onPosition(
            positionMs = exoPlayer.currentPosition.coerceAtLeast(0),
            durationMs = if (duration < 0) 0 else duration,
            bufferedPositionMs = exoPlayer.bufferedPosition.coerceAtLeast(0),
        )
        if (scheduleNext && exoPlayer.isPlaying) scheduleTicker()
    }

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            if (released) return
            dispatchPhase(playbackState, connection.current()?.isPlaying == true)
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            if (released) return
            dispatchPhase(connection.current()?.playbackState ?: Player.STATE_IDLE, isPlaying)
        }

        override fun onPlayerError(error: PlaybackException) {
            if (released) return
            listener?.onFailure(playbackFailureFor(error.errorCode))
        }
    }

    private fun dispatchPhase(playbackState: Int, isPlaying: Boolean) {
        val phase = when (playbackState) {
            Player.STATE_BUFFERING -> PlaybackPhase.Buffering
            Player.STATE_READY -> PlaybackPhase.Ready
            Player.STATE_ENDED -> PlaybackPhase.Ended
            else -> PlaybackPhase.Idle
        }
        listener?.onPhase(phase, isPlaying && phase != PlaybackPhase.Ended)
    }

    private companion object {
        const val POSITION_INTERVAL_MS = 500L
    }
}
