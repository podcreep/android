package com.podcreep.mobile.ui

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.lifecycle.ViewModel
import com.podcreep.mobile.service.MediaServiceClient
import com.podcreep.mobile.util.L
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

@HiltViewModel
class NowPlayingSheetViewModel @Inject constructor(
    private val mediaServiceClient: MediaServiceClient
) : ViewModel() {
  val log: L = L(NowPlayingSheetViewModel::class.java.simpleName)

  enum class PlayState {
    STOPPED,
    PLAYING,
    PAUSED,
    BUFFERING
  }

  data class NowPlaying (
    val playState: PlayState,
    val title: String)

  val initialNowPlaying = NowPlaying(PlayState.STOPPED, "")

  fun play() {
    mediaServiceClient.play()
  }

  fun pause() {
    mediaServiceClient.pause()
  }

  val nowPlaying = callbackFlow {
    val callbacks = mediaServiceClient.addCallback(object : MediaServiceClient.Callbacks() {
      var currState = initialNowPlaying.copy()

      override fun onMetadataChanged(metadata: MediaMetadataCompat) {
        val title = metadata.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE) ?: ""

        log.info("sending title: $title")
        currState = currState.copy(title = title)
        trySend(currState)
      }

      override fun onPlaybackStateChanged(state: PlaybackStateCompat) {
        val playState = when (state.state) {
          PlaybackStateCompat.STATE_PLAYING -> PlayState.PLAYING
          PlaybackStateCompat.STATE_PAUSED -> PlayState.PAUSED
          PlaybackStateCompat.STATE_BUFFERING -> PlayState.BUFFERING
          else -> PlayState.STOPPED
        }

        log.info("sending playState: $playState")
        currState = currState.copy(playState = playState)
        trySend(currState)
      }
    })

    awaitClose { mediaServiceClient.removeCallback(callbacks) }
  }
}
