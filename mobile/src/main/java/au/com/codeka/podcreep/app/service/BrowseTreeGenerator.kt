package au.com.codeka.podcreep.app.service

import android.net.Uri
import android.support.v4.media.MediaBrowserCompat
import androidx.media.MediaBrowserServiceCompat
import java.util.ArrayList
import android.support.v4.media.MediaDescriptionCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import au.com.codeka.podcreep.model.cache.PodcastIconCache
import au.com.codeka.podcreep.model.store.Episode
import au.com.codeka.podcreep.model.store.Podcast
import au.com.codeka.podcreep.model.store.Store
import au.com.codeka.podcreep.model.store.Subscription
import au.com.codeka.podcreep.util.observeOnce

class BrowseTreeGenerator(private val store: Store, private val iconCache: PodcastIconCache,
                          private val lifecycleOwner: LifecycleOwner) {
  private val subscriptions = store.subscriptions()

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
      "subscriptions" -> {
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
    return Uri.parse("android.resource://au.com.codeka.podcreep/drawable/$name")
  }

  private fun onLoadRootChildren(
      result: MediaBrowserServiceCompat.Result<MutableList<MediaBrowserCompat.MediaItem>>) {

    val items = ArrayList<MediaBrowserCompat.MediaItem>()
    var desc = MediaDescriptionCompat.Builder()
        .setMediaId("in_progress")
        .setTitle("In progress")
        .setIconUri(iconUrl("ic_inprogress_24dp"))
        .build()
    items.add(MediaBrowserCompat.MediaItem(desc, MediaBrowserCompat.MediaItem.FLAG_BROWSABLE))

    desc = MediaDescriptionCompat.Builder()
        .setMediaId("new_episodes")
        .setTitle("New episodes")
        .setIconUri(iconUrl("ic_new_episode_24dp"))
        .build()
    items.add(MediaBrowserCompat.MediaItem(desc, MediaBrowserCompat.MediaItem.FLAG_BROWSABLE))

    desc = MediaDescriptionCompat.Builder()
        .setMediaId("subscriptions")
        .setTitle("Subscriptions")
        .setIconUri(iconUrl("ic_subscriptions_black_24dp"))
        .build()
    items.add(MediaBrowserCompat.MediaItem(desc, MediaBrowserCompat.MediaItem.FLAG_BROWSABLE))

    result.sendResult(items)
  }

  private fun onLoadInProgressChildren(
      result: MediaBrowserServiceCompat.Result<MutableList<MediaBrowserCompat.MediaItem>>) {
    result.detach()

    store.inProgress().observeOnce(lifecycleOwner, Observer {
      episodes -> run {
      populateEpisodeResult(result, episodes)
    }
    })
  }

  private fun onLoadNewEpisodesChildren(
      result: MediaBrowserServiceCompat.Result<MutableList<MediaBrowserCompat.MediaItem>>) {
    result.detach()

    store.newEpisodes().observeOnce(lifecycleOwner, Observer {
      episodes -> run {
      populateEpisodeResult(result, episodes)
    }
    })
  }

  private fun onLoadSubscriptionsChildren(
      result: MediaBrowserServiceCompat.Result<MutableList<MediaBrowserCompat.MediaItem>>) {
    result.detach()

    subscriptions.observeOnce(lifecycleOwner, Observer {
      subscriptions -> run {
        populateSubscriptionsResult(result, subscriptions)
      }
    })
  }

  private fun populateSubscriptionsResult(
      result: MediaBrowserServiceCompat.Result<MutableList<MediaBrowserCompat.MediaItem>>,
      subscriptions: List<Subscription>) {
    val items = ArrayList<MediaBrowserCompat.MediaItem>()
    for (sub in subscriptions) {
      val desc = MediaDescriptionCompat.Builder()
          .setMediaId("sub:${sub.id}")
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
    subscriptions.observeOnce(lifecycleOwner, Observer {
      subscriptions -> run {
        val podcasts = HashMap<Long, Podcast>()
        for (sub in subscriptions) {
          val podcast = sub.podcast.value
          podcasts[podcast!!.id] = podcast
        }

        val items = ArrayList<MediaBrowserCompat.MediaItem>()
        for (ep in episodes) {
          val desc = MediaDescriptionCompat.Builder()
              .setMediaId(MediaIdBuilder().getMediaId(podcasts[ep.podcastID]!!, ep))
              .setTitle(ep.title)
              .setIconUri(iconCache.getRemoteUri(podcasts[ep.podcastID]!!))
              .build()
          items.add(MediaBrowserCompat.MediaItem(desc, MediaBrowserCompat.MediaItem.FLAG_PLAYABLE))
        }
        result.sendResult(items)
      }
    })
  }

  private fun onLoadSubscriptionChildren(
      subscriptionId: Long,
      result: MediaBrowserServiceCompat.Result<MutableList<MediaBrowserCompat.MediaItem>>) {
    result.detach()
    subscriptions.observeOnce(lifecycleOwner, Observer {
      subscriptions -> run {
        for (sub in subscriptions) {
          if (sub.id == subscriptionId) {
            store.episodes(sub.podcastID).observeOnce(lifecycleOwner, Observer {
              episodes -> run {
                val items = ArrayList<MediaBrowserCompat.MediaItem>()
                for (ep in episodes) {
                  val podcast = sub.podcast.value!!
                  val desc = MediaDescriptionCompat.Builder()
                      .setMediaId(MediaIdBuilder().getMediaId(podcast, ep))
                      .setTitle(ep.title)
                      .setIconUri(iconCache.getRemoteUri(podcast))
                      .build()
                  items.add(
                      MediaBrowserCompat.MediaItem(
                          desc, MediaBrowserCompat.MediaItem.FLAG_PLAYABLE))

                  if (items.size > 20) {
                    break
                  }
                }
                result.sendResult(items)
              }
            })
          }
        }
      }
    })
  }
}