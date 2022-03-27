package com.podcreep.app.service

import com.podcreep.model.store.Episode
import com.podcreep.model.store.Podcast
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
