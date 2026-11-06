package br.com.musicflow.player

import android.app.PendingIntent
import android.content.Intent
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import org.koin.android.ext.android.inject

class PlaybackService : MediaSessionService() {
    private val engine: Media3AudioEngine by inject()
    private val playback: MusicPlayback by inject()

    private var mediaSession: MediaSession? = null
    private var boundPlayer: ExoPlayer? = null

    override fun onCreate() {
        super.onCreate()
        val player = buildPlayer()
        boundPlayer = player
        val sessionPlayer = SessionPlayer(player, playback)
        val launch = packageManager.getLaunchIntentForPackage(packageName)
        val sessionActivity = launch?.let { intent ->
            PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
        }
        mediaSession = MediaSession.Builder(this, sessionPlayer)
            .setId(SESSION_ID)
            .apply { if (sessionActivity != null) setSessionActivity(sessionActivity) }
            .build()
        setMediaNotificationProvider(notificationProvider())
        engine.attach(player)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onDestroy() {
        val player = boundPlayer
        mediaSession?.release()
        mediaSession = null
        boundPlayer = null
        if (player != null) engine.detach(player)
        super.onDestroy()
    }

    private fun buildPlayer(): ExoPlayer {
        val attributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()
        return ExoPlayer.Builder(this)
            .setAudioAttributes(attributes, true)
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_NETWORK)
            .build()
    }

    private fun notificationProvider(): DefaultMediaNotificationProvider {
        return DefaultMediaNotificationProvider.Builder(this)
            .setChannelId(CHANNEL_ID)
            .setChannelName(R.string.playback_channel_name)
            .build()
            .also { provider -> provider.setSmallIcon(R.drawable.ic_playback_notification) }
    }

    private companion object {
        const val SESSION_ID = "musicflow.playback"
        const val CHANNEL_ID = "musicflow_playback"
    }
}
