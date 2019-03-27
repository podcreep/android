package au.com.codeka.podcreep.app.service

import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.SystemClock
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import au.com.codeka.podcreep.concurrency.TaskRunner
import au.com.codeka.podcreep.concurrency.Threads
import au.com.codeka.podcreep.model.sync.EpisodeOld
import au.com.codeka.podcreep.model.sync.PlaybackStateOld
import au.com.codeka.podcreep.model.sync.PodcastOld
import au.com.codeka.podcreep.model.sync.SubscriptionOld
import au.com.codeka.podcreep.net.HttpRequest
import au.com.codeka.podcreep.net.Server

/**
 * MediaManager manages the actual playback of the media.
 */
class MediaManager(
    private val service: MediaService,
    private val mediaSession: MediaSessionCompat,
    private val taskRunner: TaskRunner) {

  companion object {
    private const val SERVER_UPDATE_FREQUENCY_SECONDS = 20
  }

  private var _playbackState = PlaybackStateCompat.Builder()
  private var _metadata = MediaMetadataCompat.Builder()
  private var _mediaPlayer: MediaPlayer? = null

  private var _currPodcast: PodcastOld? = null
  private var _currEpisode: EpisodeOld? = null
  private var _timeToServerUpdate: Int = SERVER_UPDATE_FREQUENCY_SECONDS
  private var _updateQueued = false

  private val handler = Handler()

  val playbackState: PlaybackStateCompat.Builder
    get() = _playbackState

  init {
    updateState(false)
  }

  fun play(podcast: PodcastOld, episode: EpisodeOld) {
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

    updateState(false)
  }

  fun play() {
    _mediaPlayer?.start()
    updateState(false)
  }

  fun pause() {
    _mediaPlayer?.pause()
    updateState(true)
  }

  fun skipForward() {
    var currPos = _mediaPlayer?.currentPosition!!
    currPos += 30 * 1000
    _mediaPlayer?.seekTo(currPos)
    updateState(true)
  }

  fun skipBack() {
    var currPos = _mediaPlayer?.currentPosition!!
    currPos -= 10 * 1000
    _mediaPlayer?.seekTo(currPos)
    updateState(true)
  }

  private fun updateState(updateServer: Boolean) {
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
      _metadata.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, _mediaPlayer!!.duration.toLong())
      mediaSession.setMetadata(_metadata.build())
    }

    if (updateServer) {
      updateServerState()
    } else {
      _timeToServerUpdate --
      if (_timeToServerUpdate <= 0) {
        updateServerState()
      }
    }

    if (!_updateQueued) {
      handler.postDelayed({
        _updateQueued = false
        updateState(false)
      }, 1000)
      _updateQueued = true
    }
  }

  private fun updateServerState() {
    _timeToServerUpdate = SERVER_UPDATE_FREQUENCY_SECONDS

    taskRunner.runTask({
      val podcastID = _currPodcast?.id ?: return@runTask
      val episodeID = _currEpisode?.id ?: return@runTask
      val position = _mediaPlayer?.currentPosition ?: return@runTask
      val state = PlaybackStateOld(podcastID, episodeID, position / 1000)

      val url = "/api/podcasts/$podcastID/episodes/$episodeID/playback-state"
      val request = Server.request(url)
          .method(HttpRequest.Method.PUT)
          .body(state)
          .build()
      request.execute<SubscriptionOld>()
      // TODO: do something with subscription?
    }, Threads.BACKGROUND)
  }
}
