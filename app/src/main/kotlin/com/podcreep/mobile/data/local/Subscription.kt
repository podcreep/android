package com.podcreep.mobile.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "sub_podcasts",
    foreignKeys = [ForeignKey(
        entity = Podcast::class,
        onDelete = ForeignKey.CASCADE,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("podcastID"))],
    indices = [Index("podcastID")])
class Subscription(
    @PrimaryKey val podcastID: Long,
    @Ignore var podcast: Podcast?
) {

  constructor(podcastID: Long)
      : this(podcastID, null)
}
