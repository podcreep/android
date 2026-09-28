@file:OptIn(UnstableApi::class)

package com.podcreep.mobile.service

import android.content.Context
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.milliseconds

/** MediaManager manages the actual playback of the media using ExoPlayer. */
@Singleton
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

  class OffsetInterceptingPlayer(private val playerEngine: Player) : ForwardingPlayer(playerEngine) {
    override fun setMediaItem(mediaItem: MediaItem) {
      super.setMediaItem(mediaItem, calculateOffsetMsFor(mediaItem))
    }

    override fun setMediaItem(mediaItem: MediaItem, startPositionMs: Long) {
      // Intercept legacy commands and enforce your mandatory 25-second offset
      super.setMediaItem(mediaItem, calculateOffsetMsFor(mediaItem))
    }

    override fun setMediaItems(mediaItems: List<MediaItem>) {
      // Automatically targets the first track at your custom 25-second marker
      super.setMediaItems(mediaItems, /* startIndex= */ 0, calculateOffsetMsFor(mediaItems))
    }

    override fun setMediaItems(mediaItems: List<MediaItem>, resetPosition: Boolean) {
      super.setMediaItems(mediaItems, 0, calculateOffsetMsFor(mediaItems))
    }

    override fun setMediaItems(mediaItems: List<MediaItem>, startIndex: Int, startPositionMs: Long) {
      // Blocks external controllers from clearing or rewriting your offset
      super.setMediaItems(mediaItems, startIndex, calculateOffsetMsFor(mediaItems))
    }

    private fun calculateOffsetMsFor(mediaItems: List<MediaItem>): Long {
      var offsetMs = 0L
      mediaItems.forEach { item ->
        offsetMs = calculateOffsetMsFor(item)
      }
      return offsetMs
    }

    private fun calculateOffsetMsFor(mediaItem: MediaItem): Long {
      val mediaId = mediaItem.mediaId
      val pair = MediaIdBuilder().parse(mediaId)
      if (pair != null) {
        val episode = pair.second
        return (episode.position ?: 0) * 1000L
      }
      return 0L
    }
  }

  private val exoPlayer: ExoPlayer = ExoPlayer.Builder(context)
    .setAudioAttributes(
      AudioAttributes.Builder()
        .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
        .setUsage(C.USAGE_MEDIA)
        .build(),
      true // handle audio focus automatically via Media3 ExoPlayer
    )
    .setSeekForwardIncrementMs(30000L)
    .setSeekBackIncrementMs(10000L)
    .build()

  private val interceptingPlayer = OffsetInterceptingPlayer(exoPlayer)

  val player: Player
    get() = interceptingPlayer

  var currPodcast: Podcast? = null
  var currEpisode: Episode? = null
  private var timeToServerUpdate: Int = SERVER_UPDATE_FREQUENCY_SECONDS

  init {
    exoPlayer.addListener(object : Player.Listener {
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

  /** Called when the session changes tracks, usually due to the UI asking for it. */
  fun notifyPlay(podcast: Podcast, episode: Episode) {
    L.info("Play $podcast, $episode")
    currPodcast = podcast
    currEpisode = episode
  }

  /** Directly command the player to play the given podcast/episode. */
  fun play(podcast: Podcast, episode: Episode) {
    currPodcast = podcast
    currEpisode = episode

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

    exoPlayer.setMediaItem(mediaItem)
    exoPlayer.prepare()
    exoPlayer.play()
  }

  fun skipForward() {
    L.info("skipForward()")
    val duration = exoPlayer.duration.takeIf { it > 0 }
    val target = exoPlayer.currentPosition + 30000

    if (duration != null && target >= duration) {
      L.info("Skipping to next episode $currPodcast $currEpisode")
      val podcast = currPodcast
      val episode = currEpisode
      if (podcast != null && episode != null) {
        CoroutineScope(Dispatchers.Main).launch {
          val episodes = subscriptionsRepository.episodesOf(podcast.id).first()
          for (e in episodes) {
            L.info("       episode: $e")
          }
          if (currPodcast?.id != podcast.id || currEpisode?.id != episode.id) return@launch

          val currentIndex = episodes.indexOfFirst { it.id == episode.id }
          val nextEpisode = episodes.getOrNull(currentIndex - 1)
          if (nextEpisode != null) {
            saveCurrentPosition()



            play(podcast, nextEpisode)
            return@launch
          }
        }
      }
    }

    exoPlayer.seekTo(target)
    saveCurrentPosition()
  }

  fun skipBack() {
    val target = (exoPlayer.currentPosition - 10000).coerceAtLeast(0)
    exoPlayer.seekTo(target)
    saveCurrentPosition()
  }

  fun customAction(action: String?, extras: Bundle?) {
    when (action) {
      "skip_forward_30" -> skipForward()
      "skip_back_10", "skip_back_30" -> skipBack()
      else -> L.info("Unknown custom action: $action")
    }
  }

  private fun saveCurrentPosition() {
    val episode = currEpisode ?: return
    val position = exoPlayer.currentPosition
    episode.position = (position / 1000).toInt()
    CoroutineScope(Dispatchers.IO).launch {
      subscriptionsRepository.updateEpisode(episode)
    }
  }

  private fun updateServerState() {
    timeToServerUpdate = SERVER_UPDATE_FREQUENCY_SECONDS
    val podcastID = currPodcast?.id ?: return
    val episodeID = currEpisode?.id ?: return
    val position = exoPlayer.currentPosition
    val state = PlaybackStateJson(podcastID, episodeID, (position / 1000).toInt(), Date())
    CoroutineScope(Dispatchers.IO).launch {
      playbackStateSyncer.sync(state)
    }
  }

  private fun startServerUpdateLoop() {
    CoroutineScope(Dispatchers.Main).launch {
      while (exoPlayer.isPlaying) {
        delay(1000.milliseconds)
        if (!exoPlayer.isPlaying) break
        timeToServerUpdate--
        if (timeToServerUpdate <= 0) {
          updateServerState()
        }
        saveCurrentPosition()
      }
    }
  }
}
