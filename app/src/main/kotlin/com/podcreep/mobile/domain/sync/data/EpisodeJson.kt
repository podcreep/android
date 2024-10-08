package com.podcreep.mobile.domain.sync.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.*

@JsonClass(generateAdapter = false)
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

    @Json(name="pubDate")
    val pubDate: String,

    @Json(name="position")
    val position: Int?,

    @Json(name="isComplete")
    val isComplete: Boolean?,

    @Json(name="lastListenTime")
    val lastListenTime: Date?
)
