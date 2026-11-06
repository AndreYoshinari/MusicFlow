package br.com.musicflow.player

import androidx.media3.common.PlaybackException
import org.junit.Assert.assertEquals
import org.junit.Test

class Media3ErrorsTest {
    @Test
    fun networkAndTimeoutBecomeConnectionFailures() {
        assertEquals(
            PlaybackFailure.Connection,
            playbackFailureFor(PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED),
        )
        assertEquals(
            PlaybackFailure.Connection,
            playbackFailureFor(PlaybackException.ERROR_CODE_TIMEOUT),
        )
    }

    @Test
    fun badAddressesBecomeInvalidSources() {
        assertEquals(
            PlaybackFailure.InvalidSource,
            playbackFailureFor(PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS),
        )
        assertEquals(
            PlaybackFailure.InvalidSource,
            playbackFailureFor(PlaybackException.ERROR_CODE_PARSING_CONTAINER_MALFORMED),
        )
    }

    @Test
    fun unknownCodesStayGenericPlaybackFailures() {
        assertEquals(PlaybackFailure.Playback, playbackFailureFor(PlaybackException.ERROR_CODE_UNSPECIFIED))
    }
}
