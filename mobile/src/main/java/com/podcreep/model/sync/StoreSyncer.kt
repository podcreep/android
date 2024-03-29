package com.podcreep.model.sync

import android.content.Context
import android.util.Log
import com.podcreep.model.cache.PodcastIconCache
import com.podcreep.model.store.Episode
import com.podcreep.model.store.Podcast
import com.podcreep.model.store.Store
import com.podcreep.model.store.Subscription
import com.podcreep.model.sync.data.SubscriptionJson
import com.podcreep.net.HttpRequest
import com.podcreep.net.Server
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.text.SimpleDateFormat
import java.util.*

@JsonClass(generateAdapter = false)
data class SubscriptionsSyncPostRequest(
    @Json(name="todo")
    val todo: Boolean)

@JsonClass(generateAdapter = false)
data class SubscriptionsSyncPostResponse(
    @Json(name="subscriptions")
    val subscriptions: List<SubscriptionJson>)

/**
 * StoreSyncer is used to sync our local store with the server.
 */
class StoreSyncer(private val context: Context, s: Store, private val iconCache: PodcastIconCache) {
  companion object {
    const val TAG = "StoreSyncer"
  }

  private val store = s.localStore

  // pubDate will be in a format like: 2019-04-14T03:00:00-07:00
  private val pubDateFmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.US)

  fun sync() {
    Log.i(TAG, "Beginning sync")

    // Attempt to save all our pending playback state, if we have any.
    PlaybackStateSyncer(context, null).syncPending()

    val request = Server.request("/api/subscriptions/sync")
        .method(HttpRequest.Method.POST)
        .body(SubscriptionsSyncPostRequest(false))
        .build()
    val resp = request.execute<SubscriptionsSyncPostResponse>()

    for (sub in resp.subscriptions) {
      Log.i(TAG, "Syncing subscription '${sub.podcast.title}'")

      val podcast = Podcast(
          id = sub.podcast.id,
          title = sub.podcast.title,
          description = sub.podcast.description,
          imageUrl = sub.podcast.imageUrl)
      store.podcasts().insert(podcast)
      iconCache.refresh(podcast)

      store.subscriptions().insert(Subscription(
          podcastID = sub.podcast.id))

      if (sub.podcast.episodes != null) {
        Log.i(TAG, "  adding '${sub.podcast.episodes!!.size}' episodes.")
        for (ep in sub.podcast.episodes!!) {
          store.episodes().insert(Episode(
              id = ep.id,
              podcastID = podcast.id,
              title = ep.title,
              description = ep.description,
              mediaUrl = ep.mediaUrl,
              pubDate = pubDateFmt.parse(ep.pubDate)!!,
              position = ep.position,
              lastListenTime = ep.lastListenTime,
              isComplete = null))
        }
      } else {
        Log.i(TAG, "  no episodes?")
      }

      // TODO: any positions that aren't in podcasts.episodes, update those
    }
  }
}
