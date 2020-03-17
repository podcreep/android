package au.com.codeka.podcreep.model.store

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "podcasts")
class Podcast(
    @PrimaryKey var id: Long,
    var title: String,
    var description: String,
    var imageUrl: String)
