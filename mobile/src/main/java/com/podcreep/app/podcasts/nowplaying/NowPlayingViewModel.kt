package com.podcreep.app.podcasts.nowplaying

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.podcreep.App
import com.podcreep.app.service.MediaServiceClient

class NowPlayingViewModel(
    var metadata: MediaMetadataCompat?,
    var playbackState: PlaybackStateCompat?) {

  val albumArtUri: String?
    get() = metadata?.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI)

  val duration: Int
    get() = metadata?.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)?.toInt() ?: 0

  val progress: Int
    get() = playbackState?.position?.toInt() ?: 0

  fun onPlayPauseClick() {
    val pbs = playbackState ?: return

    if (pbs.state == PlaybackStateCompat.STATE_PLAYING) {
      App.i.mediaServiceClient.pause()
    } else {
      App.i.mediaServiceClient.play()
    }
  }

  fun onSkipBackClick() {
    App.i.mediaServiceClient.skipBack()
  }

  fun onSkipForwardClick() {
    App.i.mediaServiceClient.skipForward()
  }
}
