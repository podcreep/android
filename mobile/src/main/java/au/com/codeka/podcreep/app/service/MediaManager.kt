package au.com.codeka.podcreep.app.service

import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.SystemClock
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import au.com.codeka.podcreep.model.Episode
import au.com.codeka.podcreep.model.Podcast

/**
 * MediaManager manages the actual playback of the media.
 */
class MediaManager(
    private val service: MediaService,
    private val mediaSession: MediaSessionCompat) {

  private var _playbackState = PlaybackStateCompat.Builder()
  private var _metadata = MediaMetadataCompat.Builder()
  private var _mediaPlayer: MediaPlayer? = null

  private var _currPodcast: Podcast? = null
  private var _currEpisode: Episode? = null

  val playbackState: PlaybackStateCompat.Builder
    get() = _playbackState

  init {
    updateState()
  }

  fun play(podcast: Podcast, episode: Episode) {
    _currPodcast = podcast
    _currEpisode = episode

    var offset = 0
    if (podcast.subscription != null && podcast.subscription.positions[episode.id] != null) {
      offset = podcast.subscription.positions.getValue(episode.id)
    }

    // TODO: obviously we should do better than this!
    val uri = Uri.parse(episode.mediaUrl)
    _mediaPlayer = MediaPlayer().apply {
      setAudioStreamType(AudioManager.STREAM_MUSIC)
      setDataSource(service, uri)
      prepare()
      seekTo(offset * 1000)
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

  fun skipForward() {
    var currPos = _mediaPlayer?.currentPosition!!
    currPos += 30 * 1000
    _mediaPlayer?.seekTo(currPos)
  }

  fun skipBack() {
    var currPos = _mediaPlayer?.currentPosition!!
    currPos -= 10 * 1000
    _mediaPlayer?.seekTo(currPos)
  }

  private fun updateState() {
    _playbackState.setActions(PlaybackStateCompat.ACTION_PLAY or
        PlaybackStateCompat.ACTION_PAUSE or
        PlaybackStateCompat.ACTION_PLAY_PAUSE)

    if (_mediaPlayer == null) {
      _playbackState.setState(
          PlaybackStateCompat.STATE_NONE, 0, 1.0f, SystemClock.elapsedRealtime())
    } else {
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
    }
    mediaSession.setPlaybackState(_playbackState.build())

    if (_currEpisode != null && _currPodcast != null) {
      _metadata.putString(MediaMetadataCompat.METADATA_KEY_TITLE, _currPodcast!!.title)
      _metadata.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, _currPodcast!!.title)
      _metadata.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, _currEpisode!!.title)
      _metadata.putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, _currPodcast!!.imageUrl)
      mediaSession.setMetadata(_metadata.build())
    }
  }
}
