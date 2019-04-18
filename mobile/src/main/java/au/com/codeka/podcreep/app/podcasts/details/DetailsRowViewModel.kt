package au.com.codeka.podcreep.app.podcasts.details

import au.com.codeka.podcreep.model.store.Episode
import au.com.codeka.podcreep.model.store.Podcast
import java.text.SimpleDateFormat
import java.util.*

class DetailsRowViewModel(val podcast: Podcast, val episode: Episode) {
  private val displayDateFormat = SimpleDateFormat("dd'\n'MMM", Locale.US)

  fun getDate(): String {
    return displayDateFormat.format(episode.pubDate)
  }

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