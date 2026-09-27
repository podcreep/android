package com.podcreep.mobile.service

import android.content.Context
import android.os.Bundle
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds
import java.util.Date
import javax.inject.Inject
import androidx.core.net.toUri

/** MediaManager manages the actual playback of the media using ExoPlayer. */
class MediaManager @Inject constructor(
  @ApplicationContext val context: Context,
  private val mediaCache: EpisodeMediaCache,
  private val iconCache: PodcastIconCache,
  private val playbackStateSyncer: PlaybackStateSyncer,
  private val subscriptionsRepository: SubscriptionsRepository,
  private val settingsRepository: SettingsRepository) {

  companion object {
    private val L: L = L("MediaManager")
    private const val SERVER_UPDATE_FREQUENCY_SECONDS = 20
  }

  val player: ExoPlayer = ExoPlayer.Builder(context)
    .setAudioAttributes(
      AudioAttributes.Builder()
        .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
        .setUsage(C.USAGE_MEDIA)
        .build(),
      true // handle audio focus automatically via Media3 ExoPlayer
    )
    .build()

  var currPodcast: Podcast? = null
  var currEpisode: Episode? = null
  private var timeToServerUpdate: Int = SERVER_UPDATE_FREQUENCY_SECONDS

  init {
    player.addListener(object : Player.Listener {
      override fun onIsPlayingChanged(isPlaying: Boolean) {
        if (isPlaying) {
          startServerUpdateLoop()
        }
      }

      override fun onPositionDiscontinuity(
        oldPosition: Player.PositionInfo,
        newPosition: Player.PositionInfo,
        reason: Int
      ) {
        saveCurrentPosition()
      }
    })
  }

  fun play(podcast: Podcast, episode: Episode) {
    currPodcast = podcast
    currEpisode = episode

    val offset = (episode.position ?: 0) * 1000L
    val uri = mediaCache.getUri(podcast, episode) ?: episode.mediaUrl.toUri()

    val mediaItem = MediaItem.Builder()
      .setUri(uri)
      .setMediaId(MediaIdBuilder().getMediaId(podcast, episode))
      .setMediaMetadata(
        MediaMetadata.Builder()
          .setTitle(episode.title)
          .setArtist(podcast.title)
          .setArtworkUri(iconCache.getRemoteUriOrNull(podcast))
          .build()
      )
      .build()

    player.setMediaItem(mediaItem)
    player.prepare()
    if (offset > 0) {
      player.seekTo(offset)
    }
    player.play()
    L.info("Playing: $uri at position $offset")
  }

  fun play() {
    player.play()
  }

  fun pause() {
    player.pause()
    saveCurrentPosition()
  }

  fun skipForward() {
    val target = (player.currentPosition + 30000).coerceAtMost(player.duration.takeIf { it > 0 } ?: player.currentPosition)
    player.seekTo(target)
    saveCurrentPosition()
  }

  fun skipBack() {
    val target = (player.currentPosition - 10000).coerceAtLeast(0)
    player.seekTo(target)
    saveCurrentPosition()
  }

  fun customAction(action: String?, extras: Bundle?) {
    when (action) {
      "skip_forward_30" -> skipForward()
      "skip_back_30" -> skipBack()
      else -> L.info("Unknown custom action: $action")
    }
  }

  private fun saveCurrentPosition() {
    val episode = currEpisode ?: return
    val position = player.currentPosition
    episode.position = (position / 1000).toInt()
    CoroutineScope(Dispatchers.IO).launch {
      subscriptionsRepository.updateEpisode(episode)
    }
  }

  private fun updateServerState() {
    timeToServerUpdate = SERVER_UPDATE_FREQUENCY_SECONDS
    val podcastID = currPodcast?.id ?: return
    val episodeID = currEpisode?.id ?: return
    val position = player.currentPosition
    val state = PlaybackStateJson(podcastID, episodeID, (position / 1000).toInt(), Date())
    CoroutineScope(Dispatchers.IO).launch {
      playbackStateSyncer.sync(state)
    }
  }

  private fun startServerUpdateLoop() {
    CoroutineScope(Dispatchers.Main).launch {
      while (player.isPlaying) {
        delay(1000.milliseconds)
        if (!player.isPlaying) break
        timeToServerUpdate--
        if (timeToServerUpdate <= 0) {
          updateServerState()
        }
        saveCurrentPosition()
      }
    }
  }
}
