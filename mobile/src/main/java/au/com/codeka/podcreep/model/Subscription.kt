package au.com.codeka.podcreep.model

import au.com.codeka.podcreep.model.store.SubscriptionEntity
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import java.util.*

data class Subscription(
    val id: Long,
    val podcastID: Long,
    val podcast: Podcast?,
    val oldestUnlistenedEpisodeID: Long,
    val positions: Map<Long, Int>) {

  companion object {
    fun fromEntity(entity: SubscriptionEntity): Subscription {
      val moshi = Moshi.Builder()
          .add(KotlinJsonAdapterFactory())
          .build()
      var positions = moshi
          .adapter<Map<Long, Int>>(Map::class.java)
          .fromJson(String(entity.positionsJson, Charsets.UTF_8))
      if (positions == null) {
        positions = TreeMap()
      }
      return Subscription(entity.id, entity.podcastID, null, entity.oldestUnlistenedEpisodeID, positions)
    }

    fun fromEntity(entities: List<SubscriptionEntity>): List<Subscription> {
      val list = ArrayList<Subscription>()
      for (e in entities) {
        list.add(fromEntity(e))
      }
      return list
    }
  }

  fun toEntity(): SubscriptionEntity {
    val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    val positionsJson = moshi
        .adapter<Map<Long, Int>>(Map::class.java)
        .toJson(positions)
        .toByteArray(Charsets.UTF_8)

    return SubscriptionEntity(
        this.id, this.podcastID, this.oldestUnlistenedEpisodeID, positionsJson)
  }
}

fun List<Subscription>.toEntity(): List<SubscriptionEntity> {
  val list = ArrayList<SubscriptionEntity>()
  for (s in this) {
    list.add(s.toEntity())
  }
  return list
}