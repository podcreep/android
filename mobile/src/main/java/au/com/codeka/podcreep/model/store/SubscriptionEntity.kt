package au.com.codeka.podcreep.model.store

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "subscriptions",
    foreignKeys = [ForeignKey(
        entity = PodcastEntity::class,
        onDelete = ForeignKey.CASCADE,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("podcastID"))],
    indices = [Index("podcastID")])
class SubscriptionEntity(
    @PrimaryKey val id: Long,
    val podcastID: Long,
    val oldestUnlistenedEpisodeID: Long,
    val positionsJson: ByteArray)