package au.com.codeka.podcreep.app.podcasts.nowplaying

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import au.com.codeka.podcreep.app.service.MediaServiceClient

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
      MediaServiceClient.i.pause()
    } else {
      MediaServiceClient.i.play()
    }
  }

  fun onSkipBackClick() {
    MediaServiceClient.i.skipBack()
  }

  fun onSkipForwardClick() {
    MediaServiceClient.i.skipForward()
  }
}
