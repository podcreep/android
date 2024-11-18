package com.podcreep.mobile.domain.sync

import android.content.Context
import android.util.Log
import com.podcreep.mobile.data.SubscriptionsRepository
import com.podcreep.mobile.data.local.Episode
import com.podcreep.mobile.data.local.Podcast
import com.podcreep.mobile.data.local.Subscription
import com.podcreep.mobile.domain.cache.PodcastIconCache
import com.podcreep.mobile.domain.sync.data.SubscriptionJson
import com.podcreep.mobile.util.Server
import com.podcreep.mobile.util.await
import com.podcreep.mobile.util.toRequestBody
import com.podcreep.mobile.util.fromJson
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

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
class StoreSyncer @Inject constructor(
  private val iconCache: PodcastIconCache,
  private val playbackStateSyncer: PlaybackStateSyncer,
  private val server: Server,
  private val subscriptionsRepository: SubscriptionsRepository
) {
  companion object {
    const val TAG = "StoreSyncer"
  }

  // pubDate will be in a format like: 2019-04-14T03:00:00-07:00
  private val pubDateFmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.US)

  suspend fun sync() {
    Log.i(TAG, "Beginning sync")

    // Attempt to save all our pending playback state, if we have any.
    playbackStateSyncer.syncPending()

    val request = server.request("/api/subscriptions/sync")
        .post(SubscriptionsSyncPostRequest(false).toRequestBody())
    val resp = server.call(request).await().fromJson<SubscriptionsSyncPostResponse>()

    for (sub in resp.subscriptions) {
      Log.i(TAG, "Syncing subscription '${sub.podcast.title}'")

      val podcast = Podcast(
          id = sub.podcast.id,
          title = sub.podcast.title,
          description = sub.podcast.description,
          imageUrl = sub.podcast.imageUrl)
      subscriptionsRepository.syncPodcast(podcast)
      iconCache.refresh(podcast)

      subscriptionsRepository.syncSubscription(
        Subscription(
          podcastID = sub.podcast.id)
      )

      if (sub.podcast.episodes != null) {
        Log.i(TAG, "  adding '${sub.podcast.episodes!!.size}' episodes.")
        for (ep in sub.podcast.episodes!!) {
          subscriptionsRepository.syncEpisode(
            Episode(
              id = ep.id,
              podcastID = podcast.id,
              title = ep.title,
              description = ep.description,
              mediaUrl = ep.mediaUrl,
              pubDate = pubDateFmt.parse(ep.pubDate)!!,
              position = ep.position,
              lastListenTime = ep.lastListenTime,
              isComplete = null)
          )
        }
      } else {
        Log.i(TAG, "  no episodes?")
      }

      // TODO: any positions that aren't in podcasts.episodes, update those
    }
  }

  /** Called when we log out, we need to clear our local data store. */
  suspend fun logout() {
    subscriptionsRepository.logout()
  }
}
