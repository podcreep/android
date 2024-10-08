package com.podcreep.mobile.service

import com.podcreep.mobile.data.local.Episode
import com.podcreep.mobile.data.local.Podcast
import java.util.*

/**
 * Simple helper for building media IDs.
 */
class MediaIdBuilder {
  companion object {
    private val mapping: TreeMap<String, Pair<Podcast, Episode>> = TreeMap()
  }

  fun parse(mediaId: String): Pair<Podcast, Episode>? {
    return mapping[mediaId]
  }

  fun getMediaId(podcast: Podcast, episode: Episode): String {
    val id = Random().nextInt().toString()
    mapping[id] = Pair(podcast, episode)
    return id
  }
}
