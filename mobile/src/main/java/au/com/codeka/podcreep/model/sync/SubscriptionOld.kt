package au.com.codeka.podcreep.model.sync

import au.com.codeka.podcreep.model.store.Subscription
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import java.util.*

data class SubscriptionOld(
    val id: Long,
    val podcastID: Long,
    var podcast: PodcastOld?,
    val oldestUnlistenedEpisodeID: Long,
    val positions: Map<Long, Int>) {

  companion object {
    fun fromEntity(entity: Subscription): SubscriptionOld {
      val moshi = Moshi.Builder()
          .add(KotlinJsonAdapterFactory())
          .build()
      var positions = moshi
          .adapter<Map<Long, Int>>(Map::class.java)
          .fromJson(String(entity.positionsJson, Charsets.UTF_8))
      if (positions == null) {
        positions = TreeMap()
      }
      return SubscriptionOld(entity.id, entity.podcastID, null, entity.oldestUnlistenedEpisodeID, positions)
    }

    fun fromEntity(entities: List<Subscription>): List<SubscriptionOld> {
      val list = ArrayList<SubscriptionOld>()
      for (e in entities) {
        list.add(fromEntity(e))
      }
      return list
    }
  }

  fun toEntity(): Subscription {
    val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    val positionsJson = moshi
        .adapter<Map<Long, Int>>(Map::class.java)
        .toJson(positions)
        .toByteArray(Charsets.UTF_8)

    return Subscription(
        this.id, this.podcastID, this.oldestUnlistenedEpisodeID, positionsJson)
  }
}

fun List<SubscriptionOld>.toEntity(): List<Subscription> {
  val list = ArrayList<Subscription>()
  for (s in this) {
    list.add(s.toEntity())
  }
  return list
}