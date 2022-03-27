package com.podcreep.model.sync.data

import com.squareup.moshi.Json

data class EpisodeJson(
    @Json(name="id")
    val id: Long,

    @Json(name="podcastID")
    val podcastID: Long?,

    @Json(name="title")
    val title: String,

    @Json(name="description")
    val description: String,

    @Json(name="mediaUrl")
    val mediaUrl: String,

    @Json(name="pubDat")
    val pubDate: String
)
