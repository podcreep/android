package au.com.codeka.podcreep.model

import au.com.codeka.podcreep.model.store.PodcastEntity
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import java.util.*

data class Podcast(
    var id: Long,
    var title: String,
    var description: String,
    var imageUrl: String,
    val episodes: List<Episode>?,
    val subscription: Subscription?) {

  companion object {
    fun fromEntity(entity: PodcastEntity): Podcast {
      val moshi = Moshi.Builder()
          .add(KotlinJsonAdapterFactory())
          .build()
      var episodes = moshi
          .adapter<List<Episode>>(Episode::class.java)
          .fromJson(String(entity.episodeJson, Charsets.UTF_8))
      if (episodes == null) {
        episodes = ArrayList()
      }
      return Podcast(entity.id, entity.title, entity.description, entity.imageUrl, episodes, null)
    }
  }

  fun toEntity(): PodcastEntity {
    val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    val episodesJson = moshi
        .adapter<List<Episode>>(Episode::class.java)
        .toJson(episodes)
        .toByteArray(Charsets.UTF_8)

    return PodcastEntity(
        this.id, this.title, this.description, this.imageUrl, episodesJson)
  }

}

