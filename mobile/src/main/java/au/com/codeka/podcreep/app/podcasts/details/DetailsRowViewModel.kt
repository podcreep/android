package au.com.codeka.podcreep.app.podcasts.details

import android.util.Log
import au.com.codeka.podcreep.model.Episode
import au.com.codeka.podcreep.model.Podcast

class DetailsRowViewModel(val podcast: Podcast, val episode: Episode) {

  fun isInProgress(): Boolean {
    if (podcast.subscription == null) {
      return false
    }

    val pos = podcast.subscription.positions[episode.id]
    Log.i("DEANH", String.format("ep=%d pos=%d", episode.id, pos))
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