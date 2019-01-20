package au.com.codeka.podcreep.app.service

import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.SystemClock
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
  private var _mediaPlayer: MediaPlayer? = null

  val playbackState: PlaybackStateCompat.Builder
    get() = _playbackState

  fun play(podcast: Podcast, episode: Episode) {
    // TODO: obviously we should do better than this!
    val uri = Uri.parse(episode.mediaUrl)
    _mediaPlayer = MediaPlayer().apply {
      setAudioStreamType(AudioManager.STREAM_MUSIC)
      setDataSource(service, uri)
      prepare()
      start()
    }
    mediaSession.isActive = true

    updateState()
  }

  fun play() {
    _mediaPlayer?.start()
    updateState()
  }

  fun pause() {
    _mediaPlayer?.pause()
    updateState()
  }

  private fun updateState() {
    _playbackState.setActions(PlaybackStateCompat.ACTION_PLAY or
        PlaybackStateCompat.ACTION_PAUSE or
        PlaybackStateCompat.ACTION_PLAY_PAUSE)
    val mediaPlayer = _mediaPlayer!!
    if (mediaPlayer.isPlaying) {
      _playbackState.setState(
          PlaybackStateCompat.STATE_PLAYING,
          mediaPlayer.currentPosition.toLong(),
          1.0f,
          SystemClock.elapsedRealtime())
    } else {
      _playbackState.setState(
          PlaybackStateCompat.STATE_PAUSED,
          mediaPlayer.currentPosition.toLong(),
          1.0f,
          SystemClock.elapsedRealtime())
    }
    mediaSession.setPlaybackState(_playbackState.build())
  }
}
