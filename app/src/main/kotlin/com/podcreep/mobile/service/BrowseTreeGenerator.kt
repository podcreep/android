package com.podcreep.mobile.service

import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import androidx.media.MediaBrowserServiceCompat
import com.podcreep.mobile.data.SubscriptionsRepository
import com.podcreep.mobile.data.local.Episode
import com.podcreep.mobile.data.local.Podcast
import com.podcreep.mobile.data.local.Subscription
import com.podcreep.mobile.domain.cache.EpisodeMediaCache
import com.podcreep.mobile.domain.cache.PodcastIconCache
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class BrowseTreeGenerator @Inject constructor(
  private val subsRepo: SubscriptionsRepository, private val iconCache: PodcastIconCache,
  private val mediaCache: EpisodeMediaCache) {

  companion object {
    const val MAX_RESULT_SIZE = 16
  }

  suspend fun onLoadChildren(
      parentId: String,
      result: MediaBrowserServiceCompat.Result<MutableList<MediaBrowserCompat.MediaItem>>) {

    val parts = parentId.split(':')
    when (parts[0]) {
      "root" -> {
        onLoadRootChildren(result)
      }
      "in_progress" -> {
        onLoadInProgressChildren(result)
      }
      "new_episodes" -> {
        onLoadNewEpisodesChildren(result)
      }
      "sub_podcasts" -> {
        onLoadSubscriptionsChildren(result)
      }
      "sub" -> {
        if (parts.size == 2) {
          onLoadSubscriptionChildren(parts[1].toLong(), result)
        } else {
          // TODO: handle error
          result.sendResult(ArrayList())
        }
      } else -> {
        // TODO: this is actually an error.
        result.sendResult(ArrayList())
      }
    }
  }

  private fun iconUrl(name: String): Uri {
    return Uri.parse("android.resource://com.podcreep/drawable/$name")
  }

  private fun onLoadRootChildren(
      result: MediaBrowserServiceCompat.Result<MutableList<MediaBrowserCompat.MediaItem>>) {

    val items = ArrayList<MediaBrowserCompat.MediaItem>()
    var desc = MediaDescriptionCompat.Builder()
        .setMediaId("new_episodes")
        .setTitle("New episodes")
        .setIconUri(iconUrl("ic_browsetree_new_episode"))
        .build()
    items.add(MediaBrowserCompat.MediaItem(desc, MediaBrowserCompat.MediaItem.FLAG_BROWSABLE))

    desc = MediaDescriptionCompat.Builder()
        .setMediaId("in_progress")
        .setTitle("In progress")
        .setIconUri(iconUrl("ic_browsetree_inprogress"))
        .build()
    items.add(MediaBrowserCompat.MediaItem(desc, MediaBrowserCompat.MediaItem.FLAG_BROWSABLE))

    desc = MediaDescriptionCompat.Builder()
        .setMediaId("sub_podcasts")
        .setTitle("Subscriptions")
        .setIconUri(iconUrl("ic_browsetree_subscriptions"))
        .build()
    items.add(MediaBrowserCompat.MediaItem(desc, MediaBrowserCompat.MediaItem.FLAG_BROWSABLE))

    result.sendResult(items)
  }

  private suspend fun onLoadInProgressChildren(
      result: MediaBrowserServiceCompat.Result<MutableList<MediaBrowserCompat.MediaItem>>) {
    result.detach()

    populateEpisodeResult(result, subsRepo.inProgress().first())
  }

  private suspend fun onLoadNewEpisodesChildren(
      result: MediaBrowserServiceCompat.Result<MutableList<MediaBrowserCompat.MediaItem>>) {
    result.detach()

    populateEpisodeResult(result, subsRepo.newEpisodes().first())
  }

  private suspend fun onLoadSubscriptionsChildren(
      result: MediaBrowserServiceCompat.Result<MutableList<MediaBrowserCompat.MediaItem>>) {
    result.detach()

    populateSubscriptionsResult(result, subsRepo.subscriptions().first())
  }

  private suspend fun populateSubscriptionsResult(
      result: MediaBrowserServiceCompat.Result<MutableList<MediaBrowserCompat.MediaItem>>,
      subscriptions: List<Subscription>) {
    val items = ArrayList<MediaBrowserCompat.MediaItem>()
    for (sub in subscriptions) {
      sub.podcast?.let { podcast ->
        val desc = MediaDescriptionCompat.Builder()
          .setMediaId("sub:${sub.podcastID}")
          .setTitle(podcast.title)
          .setIconUri( iconCache.getRemoteUri(podcast))
          .build()
        items.add(MediaBrowserCompat.MediaItem(desc, MediaBrowserCompat.MediaItem.FLAG_BROWSABLE))
      }
    }
    result.sendResult(items)
  }

  private suspend fun populateEpisodeResult(
      result: MediaBrowserServiceCompat.Result<MutableList<MediaBrowserCompat.MediaItem>>,
      episodes: List<Episode>) {

    // Just combine with the current list of subscriptions.
    val podcasts = HashMap<Long, Podcast>()
    val subs = subsRepo.subscriptions().first()
    for (sub in subs) {
      val podcast = sub.podcast ?: continue
      podcasts[podcast.id] = podcast
    }

    val items = ArrayList<MediaBrowserCompat.MediaItem>()
    for (ep in episodes) {
      val podcast = podcasts[ep.podcastID] ?: continue
      val desc = populateEpisode(podcast, ep)
      items.add(MediaBrowserCompat.MediaItem(desc, MediaBrowserCompat.MediaItem.FLAG_PLAYABLE))

      if (items.size > MAX_RESULT_SIZE) {
        break
      }
    }
    result.sendResult(items)
  }

  private suspend fun onLoadSubscriptionChildren(
      podcastId: Long,
      result: MediaBrowserServiceCompat.Result<MutableList<MediaBrowserCompat.MediaItem>>) {
    result.detach()

    val podcast = subsRepo.podcast(podcastId).first()
    val items = ArrayList<MediaBrowserCompat.MediaItem>()
    for (ep in subsRepo.episodesOf(podcastId).first()) {
      val desc = populateEpisode(podcast, ep)
      items.add(
        MediaBrowserCompat.MediaItem(
          desc, MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
        )
      )

      if (items.size > MAX_RESULT_SIZE) {
        break
      }
    }
    result.sendResult(items)
  }

  private suspend fun populateEpisode(podcast: Podcast, episode: Episode): MediaDescriptionCompat {
    val extras = Bundle()

    val downloadStatus = when (mediaCache.getStatus(podcast, episode)) {
      EpisodeMediaCache.Status.Downloaded -> MediaDescriptionCompat.STATUS_DOWNLOADED
      EpisodeMediaCache.Status.InProgress -> MediaDescriptionCompat.STATUS_DOWNLOADING
      EpisodeMediaCache.Status.NotDownloaded -> MediaDescriptionCompat.STATUS_NOT_DOWNLOADED
    }
    extras.putLong(MediaDescriptionCompat.EXTRA_DOWNLOAD_STATUS, downloadStatus)

    return MediaDescriptionCompat.Builder()
        .setMediaId(MediaIdBuilder().getMediaId(podcast, episode))
        .setTitle(episode.title)
        .setIconUri(iconCache.getRemoteUri(podcast))
        .setExtras(extras)
        .build()
  }
}