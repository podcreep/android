package com.podcreep.app.service

import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import androidx.lifecycle.LifecycleOwner
import androidx.media.MediaBrowserServiceCompat
import com.podcreep.model.cache.EpisodeMediaCache
import com.podcreep.model.cache.PodcastIconCache
import com.podcreep.model.store.Episode
import com.podcreep.model.store.Podcast
import com.podcreep.model.store.Store
import com.podcreep.model.store.Subscription
import com.podcreep.util.observeOnce

class BrowseTreeGenerator(private val store: Store, private val iconCache: PodcastIconCache,
                          private val mediaCache: EpisodeMediaCache,
                          private val lifecycleOwner: LifecycleOwner) {
  private val subscriptions = store.subscriptions()

  companion object {
    const val MAX_RESULT_SIZE = 16
  }

  fun onLoadChildren(
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

  private fun onLoadInProgressChildren(
      result: MediaBrowserServiceCompat.Result<MutableList<MediaBrowserCompat.MediaItem>>) {
    result.detach()

    store.inProgress().observeOnce(lifecycleOwner) { episodes ->
      run {
        populateEpisodeResult(result, episodes)
      }
    }
  }

  private fun onLoadNewEpisodesChildren(
      result: MediaBrowserServiceCompat.Result<MutableList<MediaBrowserCompat.MediaItem>>) {
    result.detach()

    store.newEpisodes().observeOnce(lifecycleOwner) { episodes ->
      run {
        populateEpisodeResult(result, episodes)
      }
    }
  }

  private fun onLoadSubscriptionsChildren(
      result: MediaBrowserServiceCompat.Result<MutableList<MediaBrowserCompat.MediaItem>>) {
    result.detach()

    subscriptions.observeOnce(lifecycleOwner) { subscriptions ->
      run {
        populateSubscriptionsResult(result, subscriptions)
      }
    }
  }

  private fun populateSubscriptionsResult(
      result: MediaBrowserServiceCompat.Result<MutableList<MediaBrowserCompat.MediaItem>>,
      subscriptions: List<Subscription>) {
    val items = ArrayList<MediaBrowserCompat.MediaItem>()
    for (sub in subscriptions) {
      val desc = MediaDescriptionCompat.Builder()
          .setMediaId("sub:${sub.podcastID}")
          .setTitle(sub.podcast.value?.title)
          .setIconUri(iconCache.getRemoteUri(sub.podcast.value!!))
          .build()
      items.add(MediaBrowserCompat.MediaItem(desc, MediaBrowserCompat.MediaItem.FLAG_BROWSABLE))
    }
    result.sendResult(items)
  }

  private fun populateEpisodeResult(
      result: MediaBrowserServiceCompat.Result<MutableList<MediaBrowserCompat.MediaItem>>,
      episodes: List<Episode>) {
    subscriptions.observeOnce(lifecycleOwner) { subscriptions ->
      run {
        val podcasts = HashMap<Long, Podcast>()
        for (sub in subscriptions) {
          val podcast = sub.podcast.value
          podcasts[podcast!!.id] = podcast
        }

        val items = ArrayList<MediaBrowserCompat.MediaItem>()
        for (ep in episodes) {
          val desc = populateEpisode(podcasts[ep.podcastID]!!, ep)
          items.add(MediaBrowserCompat.MediaItem(desc, MediaBrowserCompat.MediaItem.FLAG_PLAYABLE))

          if (items.size > MAX_RESULT_SIZE) {
            break
          }
        }
        result.sendResult(items)
      }
    }
  }

  private fun onLoadSubscriptionChildren(
      podcastId: Long,
      result: MediaBrowserServiceCompat.Result<MutableList<MediaBrowserCompat.MediaItem>>) {
    result.detach()
    subscriptions.observeOnce(lifecycleOwner) { subscriptions ->
      run {
        for (sub in subscriptions) {
          if (sub.podcastID == podcastId) {
            store.episodes(sub.podcastID).observeOnce(lifecycleOwner) { episodes ->
              run {
                val items = ArrayList<MediaBrowserCompat.MediaItem>()
                for (ep in episodes) {
                  val podcast = sub.podcast.value!!
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
            }
          }
        }
      }
    }
  }

  private fun populateEpisode(podcast: Podcast, episode: Episode): MediaDescriptionCompat {
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