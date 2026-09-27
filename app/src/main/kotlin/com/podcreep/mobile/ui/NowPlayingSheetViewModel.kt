package com.podcreep.mobile.ui

import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
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
    val title: String,
    val imageUrl: String)

  val initialNowPlaying = NowPlaying(PlayState.STOPPED, "", "")

  fun play() {
    mediaServiceClient.play()
  }

  fun pause() {
    mediaServiceClient.pause()
  }

  val nowPlaying = callbackFlow {
    val callbacks = mediaServiceClient.addCallback(object : MediaServiceClient.Callbacks() {
      var currState = initialNowPlaying.copy()

      override fun onMetadataChanged(mediaItem: MediaItem?) {
        val metadata = mediaItem?.mediaMetadata
        val title = metadata?.title?.toString() ?: ""
        val imageUrl = metadata?.artworkUri?.toString() ?: ""

        log.info("sending title: $title")
        currState = currState.copy(title = title, imageUrl = imageUrl)
        trySend(currState)
      }

      override fun onPlaybackStateChanged(isPlaying: Boolean, playbackState: Int) {
        val playState = when {
          isPlaying -> PlayState.PLAYING
          playbackState == Player.STATE_BUFFERING -> PlayState.BUFFERING
          playbackState == Player.STATE_READY -> PlayState.PAUSED
          else -> PlayState.STOPPED
        }

        log.info("sending playState: $playState, isPlaying: $isPlaying, state: $playbackState")
        currState = currState.copy(playState = playState)
        trySend(currState)
      }
    })

    awaitClose { mediaServiceClient.removeCallback(callbacks) }
  }
}
