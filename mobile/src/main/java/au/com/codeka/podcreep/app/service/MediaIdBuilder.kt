package au.com.codeka.podcreep.app.service

import au.com.codeka.podcreep.model.sync.EpisodeOld
import au.com.codeka.podcreep.model.sync.PodcastOld
import java.util.*

/**
 * Simple helper for building media IDs.
 */
class MediaIdBuilder {

  companion object {
    private val mapping: TreeMap<String, Pair<PodcastOld, EpisodeOld>> = TreeMap()
  }

  fun parse(mediaId: String): Pair<PodcastOld, EpisodeOld>? {
    return mapping[mediaId]
  }

  fun getMediaId(podcast: PodcastOld, episode: EpisodeOld): String {
    val id = Random().nextInt().toString()
    mapping[id] = Pair(podcast, episode)
    return id
  }
}
