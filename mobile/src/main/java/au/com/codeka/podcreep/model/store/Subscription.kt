package au.com.codeka.podcreep.model.store

import androidx.room.*

@Entity(tableName = "subscriptions",
    foreignKeys = [ForeignKey(
        entity = Podcast::class,
        onDelete = ForeignKey.CASCADE,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("podcastID"))],
    indices = [Index("podcastID")])
class Subscription(
    @PrimaryKey val id: Long,
    val podcastID: Long,
    @Ignore var podcast: Podcast? = null,
    val oldestUnlistenedEpisodeID: Long,
    val positionsJson: ByteArray) {

  constructor(id: Long, podcastID: Long, oldestUnlistenedEpisodeID: Long, positionsJson: ByteArray)
      : this(id, podcastID, null, oldestUnlistenedEpisodeID, positionsJson)
}
