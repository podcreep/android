package au.com.codeka.podcreep.app.service

import au.com.codeka.podcreep.model.sync.EpisodeOld
import au.com.codeka.podcreep.model.sync.PodcastInfo
import java.util.*

/**
 * Simple helper for building media IDs.
 */
class MediaIdBuilder {

  companion object {
    private val mapping: TreeMap<String, Pair<PodcastInfo, EpisodeOld>> = TreeMap()
  }

  fun parse(mediaId: String): Pair<PodcastInfo, EpisodeOld>? {
    return mapping[mediaId]
  }

  fun getMediaId(podcast: PodcastInfo, episode: EpisodeOld): String {
    val id = Random().nextInt().toString()
    mapping[id] = Pair(podcast, episode)
    return id
  }
}
