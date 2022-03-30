package com.podcreep.app.podcasts.podcast

import androidx.lifecycle.LiveData
import com.podcreep.model.store.Episode
import com.podcreep.model.store.Podcast
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.floor
import kotlin.math.roundToInt

class PodcastRowViewModel(val podcast: LiveData<Podcast>, val episode: LiveData<Episode>) {
  private val displayDateFormat = SimpleDateFormat("dd'\n'MMM", Locale.US)

  fun getDate(): String {
    return displayDateFormat.format(episode.value?.pubDate ?: Date())
  }

  fun isInProgress(): Boolean {
    val pos = episode.value?.position
    return (pos != null && pos > 0)
  }

  fun getProgressDisplay(): String {
    val pos = episode.value?.position
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
