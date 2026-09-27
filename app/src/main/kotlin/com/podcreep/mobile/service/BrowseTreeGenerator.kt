package com.podcreep.mobile.service

import android.net.Uri
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.podcreep.mobile.data.SubscriptionsRepository
import com.podcreep.mobile.data.local.Episode
import com.podcreep.mobile.data.local.Podcast
import com.podcreep.mobile.domain.cache.EpisodeMediaCache
import com.podcreep.mobile.domain.cache.PodcastIconCache
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class BrowseTreeGenerator @Inject constructor(
  private val subsRepo: SubscriptionsRepository,
  private val iconCache: PodcastIconCache,
  private val mediaCache: EpisodeMediaCache
) {

  companion object {
    const val MAX_RESULT_SIZE = 16
  }

  suspend fun getChildren(parentId: String): MutableList<MediaItem> {
    val parts = parentId.split(':')
    return when (parts[0]) {
      "root" -> getRootChildren()
      "in_progress" -> getInProgressChildren()
      "new_episodes" -> getNewEpisodesChildren()
      "sub_podcasts" -> getSubscriptionsChildren()
      "sub" -> {
        if (parts.size == 2) {
          getSubscriptionChildren(parts[1].toLong())
        } else {
          mutableListOf()
        }
      }
      else -> mutableListOf()
    }
  }

  private fun iconUrl(name: String): Uri {
    return "android.resource://com.podcreep/drawable/$name".toUri()
  }

  private fun getRootChildren(): MutableList<MediaItem> {
    val items = mutableListOf<MediaItem>()

    items.add(
      MediaItem.Builder()
        .setMediaId("new_episodes")
        .setMediaMetadata(
          MediaMetadata.Builder()
            .setTitle("New episodes")
            .setArtworkUri(iconUrl("ic_browsetree_new_episode"))
            .setIsBrowsable(true)
            .setIsPlayable(false)
            .build()
        )
        .build()
    )

    items.add(
      MediaItem.Builder()
        .setMediaId("in_progress")
        .setMediaMetadata(
          MediaMetadata.Builder()
            .setTitle("In progress")
            .setArtworkUri(iconUrl("ic_browsetree_inprogress"))
            .setIsBrowsable(true)
            .setIsPlayable(false)
            .build()
        )
        .build()
    )

    items.add(
      MediaItem.Builder()
        .setMediaId("sub_podcasts")
        .setMediaMetadata(
          MediaMetadata.Builder()
            .setTitle("Subscriptions")
            .setArtworkUri(iconUrl("ic_browsetree_subscriptions"))
            .setIsBrowsable(true)
            .setIsPlayable(false)
            .build()
        )
        .build()
    )

    return items
  }

  private suspend fun getInProgressChildren(): MutableList<MediaItem> {
    return getEpisodeItems(subsRepo.inProgress().first())
  }

  private suspend fun getNewEpisodesChildren(): MutableList<MediaItem> {
    return getEpisodeItems(subsRepo.newEpisodes().first())
  }

  private suspend fun getSubscriptionsChildren(): MutableList<MediaItem> {
    val items = mutableListOf<MediaItem>()
    val subscriptions = subsRepo.subscriptions().first()
    for (sub in subscriptions) {
      sub.podcast?.let { podcast ->
        items.add(
          MediaItem.Builder()
            .setMediaId("sub:${sub.podcastID}")
            .setMediaMetadata(
              MediaMetadata.Builder()
                .setTitle(podcast.title)
                .setArtworkUri(iconCache.getRemoteUri(podcast))
                .setIsBrowsable(true)
                .setIsPlayable(false)
                .build()
            )
            .build()
        )
      }
    }
    return items
  }

  private suspend fun getSubscriptionChildren(podcastId: Long): MutableList<MediaItem> {
    val podcast = subsRepo.podcast(podcastId).first()
    val episodes = subsRepo.episodesOf(podcastId).first()
    val items = mutableListOf<MediaItem>()
    for (ep in episodes) {
      items.add(createEpisodeMediaItem(podcast, ep))
      if (items.size > MAX_RESULT_SIZE) break
    }
    return items
  }

  private suspend fun getEpisodeItems(episodes: List<Episode>): MutableList<MediaItem> {
    val podcasts = mutableMapOf<Long, Podcast>()
    val subs = subsRepo.subscriptions().first()
    for (sub in subs) {
      val podcast = sub.podcast ?: continue
      podcasts[podcast.id] = podcast
    }

    val items = mutableListOf<MediaItem>()
    for (ep in episodes) {
      val podcast = podcasts[ep.podcastID] ?: continue
      items.add(createEpisodeMediaItem(podcast, ep))
      if (items.size > MAX_RESULT_SIZE) break
    }
    return items
  }

  private suspend fun createEpisodeMediaItem(podcast: Podcast, episode: Episode): MediaItem {
    val uri = mediaCache.getUri(podcast, episode) ?: episode.mediaUrl.toUri()
    return MediaItem.Builder()
      .setMediaId(MediaIdBuilder().getMediaId(podcast, episode))
      .setUri(uri)
      .setMediaMetadata(
        MediaMetadata.Builder()
          .setTitle(episode.title)
          .setArtist(podcast.title)
          .setArtworkUri(iconCache.getRemoteUri(podcast))
          .setIsBrowsable(false)
          .setIsPlayable(true)
          .build()
      )
      .build()
  }
}
