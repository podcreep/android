package com.podcreep.mobile.domain.sync

import android.content.Context
import com.podcreep.mobile.Settings
import com.podcreep.mobile.domain.sync.data.PlaybackStateJson
import com.podcreep.mobile.util.L
import com.podcreep.mobile.util.MoshiHelper
import com.podcreep.mobile.util.Server
import com.podcreep.mobile.util.await
import com.podcreep.mobile.util.toRequestBody
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Types
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * PlaybackStateSyncer keeps track of where we're at with the playback state of various episodes
 * and ensures we update the server as soon as we can (i.e. immediately if possible, otherwise
 * as soon as we get network connectivity back).
 */
@Singleton
class PlaybackStateSyncer @Inject constructor(
    private val server: Server,
    private val settings: Settings,
    @ApplicationContext private val context: Context) {
  companion object {
    private val L = L("MediaService")
    private val lock = Object()
  }

  /**
   * Attempt to sync the given playback state to the server immediately. If we're unable for
   * whatever reason (no network, for example) we'll attempt to sync it at a later time.
   */
  suspend fun sync(playbackState: PlaybackStateJson) {
    synchronized(lock) {
      // If this playback state is pending, remove it (we'll either succeed or we'll fail and add the latest
      // value later on).
      val pending = loadPendingPlaybackState()
      if (pending.removeIf {
            it.podcastID == playbackState.podcastID && it.episodeID == playbackState.episodeID
          }) {
        savePendingPlaybackState(pending)
      }
    }

    syncOnly(playbackState)
  }

  /**
   * Attempt to sync all pending playback state to the server now. This should not be run on a UI thread.
   */
  suspend fun syncPending() {
    // Grab all the pending playback states, and remove all from the pending queue.
    val pending: MutableList<PlaybackStateJson>
    synchronized(lock) {
      pending = loadPendingPlaybackState()
      if (pending.isEmpty()) {
        return
      }

      savePendingPlaybackState(ArrayList())
    }

    for (playbackState in pending) {
      syncOnly(playbackState)
    }
  }

  /**
   * Sync only the given {@link PlaybackState}. If we fail to sync, we store this state for later.
   */
  private suspend fun syncOnly(playbackState: PlaybackStateJson) {
    L.info("Sending playback state: ${playbackState.episodeID} ${playbackState.position} ${playbackState.lastUpdated}")
    val url = "/api/podcasts/${playbackState.podcastID}/episodes/${playbackState.episodeID}/playback-state"
    val request = server.request(url)
      .put(playbackState.toRequestBody())
    try {
      server.call(request).await()
    } catch (e: Exception) {
      L.warning("Error syncing playback state: %s", e)

      // If there's an error syncing it now, we'll save it for later.
      synchronized(lock) {
        val pending = loadPendingPlaybackState()
        pending.add(playbackState)
        savePendingPlaybackState(pending)
      }
    }
  }

  /** Get the playback states that we're pending to sync. */
  private fun loadPendingPlaybackState(): MutableList<PlaybackStateJson> {
    val json: String = settings.get(Settings.PLAYBACK_STATE_TO_SYNC)
    if (json.isEmpty()) {
      return ArrayList()
    }

    return try {
      moshiAdapter().fromJson(json)!!
    } catch (e: JsonDataException) {
      // Ignore errors, we'll just forget about these pending states. It's not great, but there's not much we can do.
      // This typically happens if we change the data format.
      ArrayList()
    }
  }

  private fun savePendingPlaybackState(playbackState: MutableList<PlaybackStateJson>) {
    val json = moshiAdapter().toJson(playbackState)
    settings.put(Settings.PLAYBACK_STATE_TO_SYNC, json)
  }

  private fun moshiAdapter(): JsonAdapter<MutableList<PlaybackStateJson>> {
    return MoshiHelper.create().adapter(
        Types.newParameterizedType(MutableList::class.java, PlaybackStateJson::class.java))
  }
}
