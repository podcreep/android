package com.podcreep.mobile.service

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.audiofx.LoudnessEnhancer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.podcreep.mobile.R
import com.podcreep.mobile.data.SettingsRepository
import com.podcreep.mobile.data.SubscriptionsRepository
import com.podcreep.mobile.data.local.Episode
import com.podcreep.mobile.data.local.Podcast
import com.podcreep.mobile.domain.cache.EpisodeMediaCache
import com.podcreep.mobile.domain.cache.PodcastIconCache
import com.podcreep.mobile.domain.sync.PlaybackStateSyncer
import com.podcreep.mobile.domain.sync.data.PlaybackStateJson
import com.podcreep.mobile.util.L
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import kotlin.math.log10

/** MediaManager manages the actual playback of the media. */
class MediaManager @Inject constructor(
  @ApplicationContext val context: Context,
  private val mediaSession: MediaSessionCompat,
  private val mediaCache: EpisodeMediaCache,
  private val iconCache: PodcastIconCache,
  private val playbackStateSyncer: PlaybackStateSyncer,
  private val subscriptionsRepository: SubscriptionsRepository,
  private val settingsRepository: SettingsRepository) {

  companion object {
    private val L: L = L("MediaManager")

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

  // This is non-null when we're playing. We use this to boost volume past 100%.
  private var loudnessEnhancer: LoudnessEnhancer? = null

  private val handler = Handler()

  init {
    _playbackState.addCustomAction(
        PlaybackStateCompat.CustomAction.Builder(
                CUSTOM_ACTION_SKIP_BACK,
                context.resources.getString(R.string.skip_back_10),
                R.drawable.ic_rewind_10_24dp)
            .build())
    _playbackState.addCustomAction(
        PlaybackStateCompat.CustomAction.Builder(
                CUSTOM_ACTION_SKIP_FORWARD,
                context.resources.getString(R.string.skip_forward_30),
                R.drawable.ic_forward_30_24dp)
        .build())

    updateState(false)
  }

  fun play(podcast: Podcast, episode: Episode) {
    // Stop playing the current one, if any.
    if (mediaPlayer?.isPlaying == true) {
      mediaPlayer?.stop()
    }
    mediaPlayer?.release()

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
      setVolume(1.0f, 1.0f)
      setDataSource(context, uri!!)
      setOnPreparedListener {
        it.seekTo(offset * 1000)
        it.start()
        isPreparing = false
        L.info("Playing: $uri")

        loudnessEnhancer = LoudnessEnhancer(it.audioSessionId)
        updateVolumeBoost()
      }
      prepareAsync()
    }
    L.info("Preparing to play: $uri")
    isPreparing = true
    mediaSession.isActive = true

    updateState(false)
  }

  fun updateVolumeBoost() {
    CoroutineScope(Dispatchers.IO).launch {
      val volumeBoost = settingsRepository.getInt("VolumeBoost", 100) / 100f
      L.info("DEANH: volumeBoost: %f", volumeBoost)
      if (volumeBoost < 1f || volumeBoost > 3f) {
        return@launch
      }
      val gain = log10(volumeBoost) * 2000f
      loudnessEnhancer?.setTargetGain(Math.round(gain))
      L.info("DEANH: loundnessEnhancer: %s %f", if (loudnessEnhancer == null) "null" else "non-null", gain)
      loudnessEnhancer?.enabled = true
    }
  }

  fun play() {
    mediaPlayer?.start()
    updateState(false)
    updateVolumeBoost()
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
      else -> L.info("Unknown custom action: $action")
    }
  }

  private fun updateState(updateServer: Boolean) {
    val currPodcast = currPodcast
    val currEpisode = currEpisode
    val mp = mediaPlayer
    if (currEpisode != lastEpisode || currPodcast != lastPodcast || mp?.isPlaying != lastIsPlaying) {
      if (currEpisode != null && currPodcast != null && mp != null) {
        metadata.putString(MediaMetadataCompat.METADATA_KEY_TITLE, currEpisode.title)
        metadata.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, currEpisode.title)
        metadata.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, currPodcast.title)
        metadata.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, currPodcast.title)
        metadata.putString(
          MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI,
          iconCache.getRemoteUriOrNull(currPodcast).toString())
        if (mp.isPlaying) {
          metadata.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, mp.duration.toLong())
        } else {
          metadata.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, -1)
        }
      } else {
        metadata.putString(MediaMetadataCompat.METADATA_KEY_TITLE, "")
        metadata.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, "")
        metadata.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "")
        metadata.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, "")
        metadata.putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, "")
        metadata.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, -1)
      }
      mediaSession.setMetadata(metadata.build())

      lastEpisode = currEpisode
      lastPodcast = currPodcast
      lastIsPlaying = mp?.isPlaying
    }

    if (mp == null) {
      _playbackState.setState(
          PlaybackStateCompat.STATE_NONE, 0, 1.0f, SystemClock.elapsedRealtime())
      _playbackState.setActions(getSupportedActions(false))
    } else {
      _playbackState.setActions(getSupportedActions(mp.isPlaying))
      if (mp.isPlaying) {
        _playbackState.setState(
          PlaybackStateCompat.STATE_PLAYING,
          mp.currentPosition.toLong(),
          1.0f,
          SystemClock.elapsedRealtime()
        )
      } else if (isPreparing) {
        _playbackState.setState(
          PlaybackStateCompat.STATE_BUFFERING,
          mp.currentPosition.toLong(),
          1.0f,
          SystemClock.elapsedRealtime())
      } else {
        _playbackState.setState(
            PlaybackStateCompat.STATE_PAUSED,
            mp.currentPosition.toLong(),
            1.0f,
            SystemClock.elapsedRealtime())
      }
    }
    mediaSession.setPlaybackState(_playbackState.build())

    if (updateServer) {
      updateServerState()
    } else if (mp?.isPlaying == true) { // Only auto-update while playing.
      timeToServerUpdate --
      if (timeToServerUpdate <= 0) {
        updateServerState()
      }
    }

    // Update our internal store of the position.
    if (currPodcast != null && currEpisode != null && mp != null) {
      currEpisode.position = mp.currentPosition / 1000
      CoroutineScope(Dispatchers.IO).launch {
        subscriptionsRepository.updateEpisode(currEpisode)
      }
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
    val state = PlaybackStateJson(podcastID, episodeID, position / 1000, Date())
    CoroutineScope(Dispatchers.IO).launch {
      playbackStateSyncer.sync(state)
    }
  }
}
