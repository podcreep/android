package com.podcreep.mobile.ui

import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import com.podcreep.mobile.service.MediaIdBuilder
import com.podcreep.mobile.service.MediaServiceClient
import com.podcreep.mobile.util.L
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
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
    val description: String,
    val imageUrl: String,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L)

  val initialNowPlaying = NowPlaying(PlayState.STOPPED, "", "", "", 0L, 0L)

  fun play() {
    mediaServiceClient.play()
  }

  fun pause() {
    mediaServiceClient.pause()
  }

  fun skipForward() {
    mediaServiceClient.skipForward()
  }

  fun skipBack() {
    mediaServiceClient.skipBack()
  }

  val nowPlaying = callbackFlow {
    var currState = initialNowPlaying.copy()

    fun updatePositionAndDuration() {
      val pos = mediaServiceClient.getPosition()
      val dur = mediaServiceClient.getDuration()
      if (currState.positionMs != pos || currState.durationMs != dur) {
        currState = currState.copy(positionMs = pos, durationMs = dur)
        trySend(currState)
      }
    }

    val callbacks = mediaServiceClient.addCallback(object : MediaServiceClient.Callbacks() {
      override fun onMetadataChanged(mediaItem: MediaItem?) {
        if (mediaItem == null) {
          return
        }
        val pair = MediaIdBuilder().parse(mediaItem.mediaId)
        val episode = pair?.second

        val metadata = mediaItem.mediaMetadata
        val title = metadata.title?.toString() ?: ""
        val description = episode?.description ?: ""
        val imageUrl = metadata.artworkUri?.toString() ?: ""

        log.info("sending title: $title")
        val pos = mediaServiceClient.getPosition()
        val dur = mediaServiceClient.getDuration()
        currState = currState.copy(
          title = title,
          description = description,
          imageUrl = imageUrl,
          positionMs = pos,
          durationMs = dur)
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
        val pos = mediaServiceClient.getPosition()
        val dur = mediaServiceClient.getDuration()
        currState = currState.copy(playState = playState, positionMs = pos, durationMs = dur)
        trySend(currState)
      }
    })

    val tickerJob = launch {
      while (isActive) {
        if (currState.playState == PlayState.PLAYING) {
          updatePositionAndDuration()
        }
        delay(500)
      }
    }

    awaitClose {
      tickerJob.cancel()
      mediaServiceClient.removeCallback(callbacks)
    }
  }
}
