package au.com.codeka.podcreep.model.sync

import android.util.Log
import au.com.codeka.podcreep.model.store.Episode
import au.com.codeka.podcreep.model.store.Podcast
import au.com.codeka.podcreep.model.store.Store
import au.com.codeka.podcreep.model.store.Subscription
import au.com.codeka.podcreep.net.HttpRequest
import au.com.codeka.podcreep.net.Server
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import java.text.SimpleDateFormat
import java.util.*

/**
 * StoreSyncer is used to sync our local store with the server.
 */
class StoreSyncer(s: Store) {
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

      val podcast = sub.podcast!!
      store.podcasts().insert(Podcast(
          id = podcast.id,
          title = podcast.title,
          description = podcast.description,
          imageUrl = podcast.imageUrl))

      store.subscriptions().insert(Subscription(
          id = sub.id,
          podcastID = sub.podcastID,
          oldestUnlistenedEpisodeID = sub.oldestUnlistenedEpisodeID,
          positionsJson = positionsJson))

      if (podcast.episodes != null) {
        for (ep in podcast.episodes) {
          store.episodes().insert(Episode(
              id = ep.id,
              podcastID = podcast.id,
              title = ep.title,
              description = ep.description,
              mediaUrl = ep.mediaUrl,
              pubDate = pubDateFmt.parse(ep.pubDate),
              position = sub.positions[ep.id]))
        }
      }

      // TODO: any positions that aren't in podcasts.episodes, update those

      // TODO: we should compare our position with the servers and only take the latest value
      // (currently we don't store when the position was changed, but if the android client is
      // offline while we're listening, for example, we don't update the server).
    }
  }
}
