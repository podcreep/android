package au.com.codeka.podcreep.app.service

import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import au.com.codeka.podcreep.model.Episode
import au.com.codeka.podcreep.model.Podcast

/**
 * MediaManager manages the actual playback of the media.
 */
class MediaManager(
    private val service: MediaService,
    private val mediaSession: MediaSessionCompat) {

  private var _playbackState = PlaybackStateCompat.Builder()

  val playbackState: PlaybackStateCompat.Builder
    get() = _playbackState

  fun play(podcast: Podcast, episode: Episode) {
    // TODO: obviously we should do better than this!
    val uri = Uri.parse(episode.mediaUrl)
    val mediaPlayer: MediaPlayer? = MediaPlayer().apply {
      setAudioStreamType(AudioManager.STREAM_MUSIC)
      setDataSource(service, uri)
      prepare()
      start()
    }
    mediaSession.isActive = true

    updateState()
  }

  private fun updateState() {
    _playbackState.setActions(PlaybackStateCompat.ACTION_PLAY or
        PlaybackStateCompat.ACTION_PAUSE or
        PlaybackStateCompat.ACTION_PLAY_PAUSE)
    mediaSession.setPlaybackState(_playbackState.build())
  }
}
