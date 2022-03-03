package au.com.codeka.podcreep.model.store

import androidx.lifecycle.MutableLiveData
import androidx.room.*

@Entity(tableName = "sub_podcasts",
    foreignKeys = [ForeignKey(
        entity = Podcast::class,
        onDelete = ForeignKey.CASCADE,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("podcastID"))],
    indices = [Index("podcastID")])
class Subscription(
    @PrimaryKey val id: Long,
    val podcastID: Long,
    @Ignore var podcast: MutableLiveData<Podcast>,
    val positionsJson: ByteArray) {

  constructor(id: Long, podcastID: Long, positionsJson: ByteArray)
      : this(id, podcastID, MutableLiveData(), positionsJson)
}
