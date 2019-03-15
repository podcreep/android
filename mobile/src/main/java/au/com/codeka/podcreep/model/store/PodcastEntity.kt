package au.com.codeka.podcreep.model.store

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "podcast")
class PodcastEntity(
    @PrimaryKey var id: Long,
    var title: String,
    var description: String,
    var imageUrl: String,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB) var episodeJson: ByteArray)
