package au.com.codeka.podcreep.model

import au.com.codeka.podcreep.model.store.PodcastEntity
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi

data class Podcast(
    var id: Long,
    var title: String,
    var description: String,
    var imageUrl: String,
    val episodes: List<Episode>?,
    val subscription: Subscription?) {

  fun toEntity(): PodcastEntity {
    val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    val episodesJson = moshi
        .adapter<List<Episode>>(Episode::class.java)
        .toJson(episodes)
        .toByteArray()

    return PodcastEntity(
        this.id, this.title, this.description, this.imageUrl, episodesJson)
  }

}

