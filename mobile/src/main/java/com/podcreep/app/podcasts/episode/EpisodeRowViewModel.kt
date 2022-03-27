package com.podcreep.app.podcasts.episode

import android.view.View
import com.podcreep.R
import com.podcreep.model.cache.EpisodeMediaCache
import com.podcreep.model.store.Episode
import com.podcreep.model.store.Podcast
import kotlin.math.floor
import kotlin.math.roundToInt

/** View model for showing an episode in a row of {@link BaseEpisodeListLayout}. */
class EpisodeRowViewModel(val podcast: Podcast, val episode: Episode,
                          private val mediaCache: EpisodeMediaCache) {
  fun isInProgress(): Boolean {
    val pos = episode.position
    return (pos != null && pos > 0)
  }

  fun getProgressDisplay(): String {
    val pos = episode.position
    if (pos == null || pos <= 0) {
      return "--:--"
    }

    val seconds = pos.toDouble()
    val min = floor(seconds / 60)
    val sec = (seconds - (min * 60)).roundToInt()
    var str = ""
    if (min < 10) {
      str += "0"
    }
    str += String.format("%d:", min.roundToInt())
    if (sec < 10) {
      str += "0"
    }
    str += sec
    return str
  }

  val statusIconResId: Int
    get() = when (mediaCache.getStatus(podcast, episode)) {
      EpisodeMediaCache.Status.Downloaded -> R.drawable.ic_download_24dp
      EpisodeMediaCache.Status.InProgress -> R.drawable.ic_dots_horz_24dp
      else -> R.drawable.ic_download_24dp // We'll be hidden anyway, so doesn't matter
    }

  val statusTextResId: Int
    get() = when (mediaCache.getStatus(podcast, episode)) {
      EpisodeMediaCache.Status.Downloaded -> R.string.status_downloaded
      EpisodeMediaCache.Status.InProgress -> R.string.status_downloading
      else -> R.string.status_downloaded // We'll be hidden anyway, so doesn't matter.
    }

  val statusIconVisibility: Int
    get() = when(mediaCache.getStatus(podcast, episode)) {
      EpisodeMediaCache.Status.NotDownloaded -> View.GONE
      else -> View.VISIBLE
    }
}
