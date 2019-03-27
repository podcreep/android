package au.com.codeka.podcreep.app.podcasts.details

import au.com.codeka.podcreep.model.sync.EpisodeOld
import au.com.codeka.podcreep.model.sync.PodcastOld
import java.text.SimpleDateFormat
import java.util.*

class DetailsRowViewModel(val podcast: PodcastOld, val episode: EpisodeOld) {
  private val epDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
  private val displayDateFormat = SimpleDateFormat("dd'\n'MMM", Locale.US)

  fun getDate(): String {
    return displayDateFormat.format(epDateFormat.parse(episode.pubDate))
  }

  fun isInProgress(): Boolean {
    if (podcast.subscription == null) {
      return false
    }

    val pos = podcast.subscription.positions[episode.id]
    return (pos != null && pos > 0)
  }

  fun getProgressDisplay(): String {
    if (podcast.subscription == null) {
      return "--:--"
    }

    val pos = podcast.subscription.positions.get(episode.id)
    if (pos == null || pos <= 0) {
      return "--:--"
    }

    val seconds = pos.toDouble()
    val min = Math.floor(seconds / 60)
    val sec = Math.round(seconds - (min * 60))
    var str = ""
    if (min < 10) {
      str += "0"
    }
    str += String.format("%d:", Math.round(min))
    if (sec < 10) {
      str += "0"
    }
    str += sec
    return str
  }

}