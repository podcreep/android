package au.com.codeka.podcreep.app.podcasts.subscriptions

import androidx.lifecycle.LiveData
import au.com.codeka.podcreep.model.store.Episode
import au.com.codeka.podcreep.model.store.Podcast
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.floor
import kotlin.math.roundToInt

class EpisodeRowViewModel(val podcast: LiveData<Podcast>, val episode: Episode) {
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
}
