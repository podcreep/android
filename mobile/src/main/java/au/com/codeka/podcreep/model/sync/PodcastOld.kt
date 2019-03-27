package au.com.codeka.podcreep.model.sync

import au.com.codeka.podcreep.model.store.Podcast
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import java.util.*

data class PodcastOld(
    var id: Long,
    var title: String,
    var description: String,
    var imageUrl: String,
    val episodes: List<EpisodeOld>?,
    val subscription: SubscriptionOld?) {

  companion object {
    fun fromEntity(entity: Podcast): PodcastOld {
      val moshi = Moshi.Builder()
          .add(KotlinJsonAdapterFactory())
          .build()
      var episodes = moshi
          .adapter<List<EpisodeOld>>(EpisodeOld::class.java)
          .fromJson(String(entity.episodeJson, Charsets.UTF_8))
      if (episodes == null) {
        episodes = ArrayList()
      }
      return PodcastOld(entity.id, entity.title, entity.description, entity.imageUrl, episodes, null)
    }
  }

  fun toEntity(): Podcast {
    val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    val episodesJson = moshi
        .adapter<List<EpisodeOld>>(EpisodeOld::class.java)
        .toJson(episodes)
        .toByteArray(Charsets.UTF_8)

    return Podcast(
        this.id, this.title, this.description, this.imageUrl, episodesJson)
  }

}

