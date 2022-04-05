package com.podcreep.model.store

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.*

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
    var isComplete: Boolean?)
