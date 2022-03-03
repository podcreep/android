package au.com.codeka.podcreep.model.sync

import android.content.Context
import android.util.Log
import au.com.codeka.podcreep.model.cache.PodcastIconCache
import au.com.codeka.podcreep.model.store.Episode
import au.com.codeka.podcreep.model.store.Podcast
import au.com.codeka.podcreep.model.store.Store
import au.com.codeka.podcreep.model.store.Subscription
import au.com.codeka.podcreep.net.HttpRequest
import au.com.codeka.podcreep.net.Server
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.temporal.TemporalUnit
import java.util.*

/**
 * StoreSyncer is used to sync our local store with the server.
 */
class StoreSyncer(private val context: Context, s: Store, private val iconCache: PodcastIconCache) {
  companion object {
    const val TAG = "StoreSyncer"
  }

  private val store = s.localStore

  private val moshi = Moshi.Builder()
      .add(KotlinJsonAdapterFactory())
      .build()

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
      Log.i(TAG, "Syncing subscription '${sub.podcast?.title}'")

      val positionsJson = moshi
          .adapter<Map<Long, Int>>(Map::class.java)
          .toJson(sub.positions)
          .toByteArray(Charsets.UTF_8)

      val podcast = Podcast(
          id = sub.podcast!!.id,
          title = sub.podcast!!.title,
          description = sub.podcast!!.description,
          imageUrl = sub.podcast!!.imageUrl)
      store.podcasts().insert(podcast)
      iconCache.refresh(podcast)

      store.subscriptions().insert(Subscription(
          id = sub.id,
          podcastID = sub.podcastID,
          positionsJson = positionsJson))

      if (sub.podcast!!.episodes != null) {
        Log.i(TAG, "  adding '${sub.podcast!!.episodes!!.size}' episodes.")
        for (ep in sub.podcast!!.episodes!!) {
          store.episodes().insert(Episode(
              id = ep.id,
              podcastID = podcast.id,
              title = ep.title,
              description = ep.description,
              mediaUrl = ep.mediaUrl,
              pubDate = pubDateFmt.parse(ep.pubDate)!!,
              position = sub.positions[ep.id]))
        }
      } else {
        Log.i(TAG, "  no episodes?")
      }

      // TODO: any positions that aren't in podcasts.episodes, update those
    }
  }
}
