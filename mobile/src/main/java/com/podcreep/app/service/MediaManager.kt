package com.podcreep.app.service

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
import com.podcreep.R
import com.podcreep.concurrency.TaskRunner
import com.podcreep.concurrency.Threads
import com.podcreep.model.cache.EpisodeMediaCache
import com.podcreep.model.store.Episode
import com.podcreep.model.store.Podcast
import com.podcreep.model.store.Store
import com.podcreep.model.sync.data.PlaybackStateJson
import com.podcreep.model.sync.PlaybackStateSyncer

/** MediaManager manages the actual playback of the media. */
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
  val playbackState: PlaybackStateCompat.Builder
    get() = _playbackState

  private var metadata = MediaMetadataCompat.Builder()
  private var mediaPlayer: MediaPlayer? = null
  private var isPreparing: Boolean = false

  private var currPodcast: Podcast? = null
  private var currEpisode: Episode? = null
  private var timeToServerUpdate: Int = SERVER_UPDATE_FREQUENCY_SECONDS
  private var updateQueued = false

  // We update the metadata at the same time as playback state, but we don't want to update the
  // metadata over and over if nothing changes, so this keeps track of the last podcast/episode
  // that we updated the metadata for.
  private var lastPodcast: Podcast? = null
  private var lastEpisode: Episode? = null
  private var lastIsPlaying: Boolean? = null

  private val handler = Handler()

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
    currPodcast = podcast
    currEpisode = episode

    val offset = episode.position ?: 0

    // TODO: if we haven't downloaded the media yet, instead of just using the live one, we should
    // start downloading it now and then play from there.
    val uri = mediaCache.getUri(podcast, episode) ?: Uri.parse(episode.mediaUrl)
    mediaPlayer = MediaPlayer().apply {
      setAudioAttributes(AudioAttributes.Builder()
          .setUsage(AudioAttributes.USAGE_MEDIA)
          .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
          .build())
      setDataSource(service, uri!!)
      setOnPreparedListener {
        it.seekTo(offset * 1000)
        it.start()
        isPreparing = false
        Log.i(TAG, "Playing: $uri")
      }
      prepareAsync()
    }
    Log.i(TAG, "Preparing to play: $uri")
    isPreparing = true
    mediaSession.isActive = true

    updateState(false)
  }

  fun play() {
    mediaPlayer?.start()
    updateState(false)
  }

  fun pause() {
    mediaPlayer?.pause()
    updateState(true)
  }

  fun skipForward() {
    var currPos = mediaPlayer?.currentPosition!!
    currPos += 30 * 1000
    mediaPlayer?.seekTo(currPos)
    updateState(true)
  }

  fun skipBack() {
    var currPos = mediaPlayer?.currentPosition!!
    currPos -= 10 * 1000
    mediaPlayer?.seekTo(currPos)
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
    val currPodcast = currPodcast
    val currEpisode = currEpisode
    val mp = mediaPlayer
    if (currEpisode != lastEpisode && currPodcast != lastPodcast && mp?.isPlaying != lastIsPlaying) {
      if (currEpisode != null && currPodcast != null && mp != null) {
        metadata.putString(MediaMetadataCompat.METADATA_KEY_TITLE, currPodcast.title)
        metadata.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, currPodcast.title)
        metadata.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, currEpisode.title)
        metadata.putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, currPodcast.imageUrl)
        if (mp.isPlaying) {
          metadata.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, mp.duration.toLong())
        } else {
          metadata.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, -1)
        }
      } else {
        metadata.putString(MediaMetadataCompat.METADATA_KEY_TITLE, "")
        metadata.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, "")
        metadata.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, "")
        metadata.putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, "")
        metadata.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, -1)
      }
      mediaSession.setMetadata(metadata.build())

      lastEpisode = currEpisode
      lastPodcast = currPodcast
      lastIsPlaying = mp?.isPlaying
    }

    val mediaPlayer = mediaPlayer
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
          SystemClock.elapsedRealtime()
        )
      } else if (isPreparing) {
        _playbackState.setState(
          PlaybackStateCompat.STATE_BUFFERING,
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
      timeToServerUpdate --
      if (timeToServerUpdate <= 0) {
        updateServerState()
      }
    }

    // Update our internal store of the position.
    if (currPodcast != null && currEpisode != null) {
      currEpisode.position = this.mediaPlayer!!.currentPosition / 1000
      taskRunner.runTask({ store.localStore.episodes().insert(currEpisode) }, Threads.BACKGROUND)
    }

    if (!updateQueued) {
      handler.postDelayed({
        updateQueued = false
        updateState(false)
      }, 1000)
      updateQueued = true
    }
  }

  @PlaybackStateCompat.Actions
  private fun getSupportedActions(isPlaying: Boolean): Long {
    val playbackState = PlaybackStateCompat.ACTION_PLAY or
        PlaybackStateCompat.ACTION_PAUSE or
        PlaybackStateCompat.ACTION_PLAY_PAUSE or
        PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID

    return playbackState
  }

  private fun updateServerState() {
    timeToServerUpdate = SERVER_UPDATE_FREQUENCY_SECONDS

    val podcastID = currPodcast?.id ?: return
    val episodeID = currEpisode?.id ?: return
    val position = mediaPlayer?.currentPosition ?: return
    val state = PlaybackStateJson(podcastID, episodeID, position / 1000)
    PlaybackStateSyncer(service, taskRunner).sync(state)
  }
}
