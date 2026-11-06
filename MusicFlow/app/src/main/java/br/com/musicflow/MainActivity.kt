package br.com.musicflow

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import br.com.musicflow.designsystem.theme.MusicFlowTheme
import br.com.musicflow.player.MusicPlayback
import br.com.musicflow.player.PlaybackSession
import br.com.musicflow.ui.MusicFlowRoot
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    private val playbackSession: PlaybackSession by inject()
    private val playback: MusicPlayback by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        setContent {
            MusicFlowTheme {
                MusicFlowRoot(
                    playbackSession = playbackSession,
                    playback = playback,
                    onTogglePlayback = {
                        if (playback.playbackState.value.isPlaying) playback.pause() else playback.resume()
                    },
                )
            }
        }
    }
}
