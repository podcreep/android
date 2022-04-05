package com.podcreep.model.store

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
    @PrimaryKey val podcastID: Long,
    @Ignore var podcast: MutableLiveData<Podcast>) {

  constructor(podcastID: Long)
      : this(podcastID, MutableLiveData())
}
