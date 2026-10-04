package com.podcreep.mobile.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@Entity(tableName = "episodes",
    foreignKeys = [ForeignKey(
        entity = Podcast::class,
        parentColumns = ["id"],
        childColumns = ["podcastID"],
        onDelete = CASCADE)],
    indices = [Index("podcastID")])
data class Episode(
    @PrimaryKey var id: Long,
    var podcastID: Long,
    var title: String,
    var description: String,
    var mediaUrl: String,
    var pubDate: Date,
    var position: Int?,
    var isComplete: Boolean?,
    var lastListenTime: Date?,
    @ColumnInfo(name = "duration") var durationSecs: Long?
) {
  val duration: Duration?
    get() = durationSecs?.seconds
}
