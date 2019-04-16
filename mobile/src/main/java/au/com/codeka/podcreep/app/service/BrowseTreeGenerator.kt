package au.com.codeka.podcreep.app.service

import android.net.Uri
import android.support.v4.media.MediaBrowserCompat
import androidx.media.MediaBrowserServiceCompat
import java.util.ArrayList
import android.support.v4.media.MediaDescriptionCompat
import au.com.codeka.podcreep.concurrency.TaskRunner
import au.com.codeka.podcreep.concurrency.Threads
import au.com.codeka.podcreep.model.sync.SubscriptionInfo
import au.com.codeka.podcreep.net.HttpRequest
import au.com.codeka.podcreep.net.Server


class BrowseTreeGenerator(private val taskRunner: TaskRunner) {
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
        if (parts.size == 1) {
          onLoadSubscriptionChildren(parts[1], result)
        } else {
          // TODO: handle error
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

    // TODO: this should be stored locally, etc etc.
    taskRunner.runTask({
      val request = Server.request("/api/subscriptions")
          .method(HttpRequest.Method.GET)
          .build()
      var resp = request.execute<List<SubscriptionInfo>>()
      taskRunner.runTask({
        val items = ArrayList<MediaBrowserCompat.MediaItem>()
        for (sub in resp) {
          val desc = MediaDescriptionCompat.Builder()
              .setMediaId("sub:${sub.id}")
              .setTitle(sub.podcast?.title)
              .setIconUri(Uri.parse(sub.podcast?.imageUrl))
              .build()
          items.add(MediaBrowserCompat.MediaItem(desc, MediaBrowserCompat.MediaItem.FLAG_BROWSABLE))
        }
        result.sendResult(items)
      }, Threads.UI)
    }, Threads.BACKGROUND)
  }

  private fun onLoadSubscriptionChildren(
      subscriptionId: String,
      result: MediaBrowserServiceCompat.Result<MutableList<MediaBrowserCompat.MediaItem>>) {
    // TODO: get the subscription, and get the items.
    result.sendResult(ArrayList())
  }
}