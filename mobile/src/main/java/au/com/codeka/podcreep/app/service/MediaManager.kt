package au.com.codeka.podcreep.app.service

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import au.com.codeka.podcreep.R
import au.com.codeka.podcreep.concurrency.TaskRunner
import au.com.codeka.podcreep.concurrency.Threads
import au.com.codeka.podcreep.model.cache.EpisodeMediaCache
import au.com.codeka.podcreep.model.store.Episode
import au.com.codeka.podcreep.model.store.Podcast
import au.com.codeka.podcreep.model.store.Store
import au.com.codeka.podcreep.model.sync.PlaybackState
import au.com.codeka.podcreep.model.sync.PlaybackStateSyncer
import au.com.codeka.podcreep.model.sync.SubscriptionInfo
import au.com.codeka.podcreep.net.HttpRequest
import au.com.codeka.podcreep.net.Server
import java.io.FileDescriptor

/**
 * MediaManager manages the actual playback of the media.
 */
class MediaManager(
    private val service: MediaService,
    private val mediaSession: MediaSessionCompat,
    private val taskRunner: TaskRunner,
    private val mediaCache: EpisodeMediaCache,
    private val store: Store) {

  companion object {
    private const val TAG = "MediaManager"

    private const val SERVER_UPDATE_FREQUENCY_SECONDS = 20

    private const val CUSTOM_ACTION_SKIP_FORWARD = "skip_forward_30"
    private const val CUSTOM_ACTION_SKIP_BACK = "skip_back_30"
  }

  private var _playbackState = PlaybackStateCompat.Builder()
  private var _metadata = MediaMetadataCompat.Builder()
  private var _mediaPlayer: MediaPlayer? = null

  private var _currPodcast: Podcast? = null
  private var _currEpisode: Episode? = null
  private var _timeToServerUpdate: Int = SERVER_UPDATE_FREQUENCY_SECONDS
  private var _updateQueued = false

  // We update the metadata at the same time as playback state, but we don't want to update the
  // metadata over and over if nothing changes, so this keeps track of the last podcast/episode
  // that we updated the metadata for.
  private var _lastPodcast: Podcast? = null
  private var _lastEpisode: Episode? = null

  private val handler = Handler()

  val playbackState: PlaybackStateCompat.Builder
    get() = _playbackState

  init {
    _playbackState.addCustomAction(
        PlaybackStateCompat.CustomAction.Builder(
                CUSTOM_ACTION_SKIP_BACK,
                service.resources.getString(R.string.skip_back_10),
                R.drawable.ic_rewind_10_24dp)
            .build())
    _playbackState.addCustomAction(
        PlaybackStateCompat.CustomAction.Builder(
                CUSTOM_ACTION_SKIP_FORWARD,
                service.resources.getString(R.string.skip_forward_30),
                R.drawable.ic_forward_30_24dp)
        .build())

    updateState(false)
  }

  fun play(podcast: Podcast, episode: Episode) {
    _currPodcast = podcast
    _currEpisode = episode

    val offset = episode.position ?: 0

    // TODO: if we haven't downloaded the media yet, instead of just using the live one, we should
    // start downloading it now and then play from there.
    val uri = mediaCache.getUri(podcast, episode) ?: Uri.parse(episode.mediaUrl)
    _mediaPlayer = MediaPlayer().apply {
      setAudioAttributes(AudioAttributes.Builder()
          .setUsage(AudioAttributes.USAGE_MEDIA)
          .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
          .build())
      setDataSource(service, uri!!)
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

  fun customAction(action: String?, extras: Bundle?) {
    when(action) {
      CUSTOM_ACTION_SKIP_FORWARD -> skipForward()
      CUSTOM_ACTION_SKIP_BACK -> skipBack()
      else -> Log.i(TAG, "Unknown custom action: $action")
    }
  }

  private fun updateState(updateServer: Boolean) {
    val currPodcast = _currPodcast
    val currEpisode = _currEpisode
    if (currEpisode != _lastEpisode && currPodcast != _lastPodcast) {
      if (currEpisode != null && currPodcast != null) {
        _metadata.putString(MediaMetadataCompat.METADATA_KEY_TITLE, currPodcast.title)
        _metadata.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, currPodcast.title)
        _metadata.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, currEpisode.title)
        _metadata.putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, currPodcast.imageUrl)
        _metadata.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, _mediaPlayer!!.duration.toLong())
      } else {
        _metadata.putString(MediaMetadataCompat.METADATA_KEY_TITLE, "")
        _metadata.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, "")
        _metadata.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, "")
        _metadata.putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, "")
        _metadata.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, -1)
      }
      mediaSession.setMetadata(_metadata.build())

      _lastEpisode = currEpisode
      _lastPodcast = currPodcast
    }

    val mediaPlayer = _mediaPlayer
    if (mediaPlayer == null) {
      _playbackState.setState(
          PlaybackStateCompat.STATE_NONE, 0, 1.0f, SystemClock.elapsedRealtime())
      _playbackState.setActions(getSupportedActions(false))
    } else {
      _playbackState.setActions(getSupportedActions(mediaPlayer.isPlaying))
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

    if (updateServer) {
      updateServerState()
    } else {
      _timeToServerUpdate --
      if (_timeToServerUpdate <= 0) {
        updateServerState()
      }
    }

    // Update our internal store of the position.
    if (currPodcast != null && currEpisode != null) {
      currEpisode.position = _mediaPlayer!!.currentPosition / 1000
      taskRunner.runTask({ store.localStore.episodes().insert(currEpisode) }, Threads.BACKGROUND)
    }

    if (!_updateQueued) {
      handler.postDelayed({
        _updateQueued = false
        updateState(false)
      }, 1000)
      _updateQueued = true
    }
  }

  @PlaybackStateCompat.Actions
  private fun getSupportedActions(isPlaying: Boolean): Long {
    val playbackState = PlaybackStateCompat.ACTION_PLAY or
        PlaybackStateCompat.ACTION_PAUSE or
        PlaybackStateCompat.ACTION_PLAY_PAUSE or
        PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID or
        PlaybackStateCompat.ACTION_FAST_FORWARD or
        PlaybackStateCompat.ACTION_REWIND or
        PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS

    return playbackState
  }

  private fun updateServerState() {
    _timeToServerUpdate = SERVER_UPDATE_FREQUENCY_SECONDS

    val podcastID = _currPodcast?.id ?: return
    val episodeID = _currEpisode?.id ?: return
    val position = _mediaPlayer?.currentPosition ?: return
    val state = PlaybackState(podcastID, episodeID, position / 1000)
    PlaybackStateSyncer(service, taskRunner).sync(state)
  }
}
