package au.com.codeka.podcreep.app.service

import android.net.Uri
import android.support.v4.media.MediaBrowserCompat
import androidx.media.MediaBrowserServiceCompat
import java.util.ArrayList
import android.support.v4.media.MediaDescriptionCompat
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import au.com.codeka.podcreep.model.store.Store
import au.com.codeka.podcreep.model.store.Subscription
import au.com.codeka.podcreep.util.observeOnce

class BrowseTreeGenerator(private val store: Store, private val lifecycleOwner: LifecycleOwner) {
  private val subscriptions = store.subscriptions()

  fun onLoadChildren(
      parentId: String,
      result: MediaBrowserServiceCompat.Result<MutableList<MediaBrowserCompat.MediaItem>>) {

    val parts = parentId.split(':')
    when (parts[0]) {
      "root" -> {
        onLoadRootChildren(result)
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
    val desc = MediaDescriptionCompat.Builder()
        .setMediaId("subscriptions")
        .setTitle("Subscriptions")
        .setIconUri(iconUrl("ic_subscriptions_black_24dp"))
        .build()
    items.add(MediaBrowserCompat.MediaItem(desc, MediaBrowserCompat.MediaItem.FLAG_BROWSABLE))

    result.sendResult(items)
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
          .setIconUri(Uri.parse(sub.podcast.value?.imageUrl))
          .build()
      items.add(MediaBrowserCompat.MediaItem(desc, MediaBrowserCompat.MediaItem.FLAG_BROWSABLE))
    }
    result.sendResult(items)
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
                      .setIconUri(Uri.parse(podcast.imageUrl))
                      .build()
                  items.add(MediaBrowserCompat.MediaItem(desc, MediaBrowserCompat.MediaItem.FLAG_PLAYABLE))
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