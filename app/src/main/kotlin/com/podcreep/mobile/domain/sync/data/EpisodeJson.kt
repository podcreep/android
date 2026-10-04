package com.podcreep.mobile.domain.sync.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.*

@JsonClass(generateAdapter = false)
data class EpisodeJson(
    @param:Json(name="id")
    val id: Long,

    @param:Json(name="podcastID")
    val podcastID: Long?,

    @param:Json(name="title")
    val title: String,

    @param:Json(name="description")
    val description: String,

    @param:Json(name="mediaUrl")
    val mediaUrl: String,

    @param:Json(name="pubDate")
    val pubDate: String,

    @param:Json(name="position")
    val position: Int?,

    @param:Json(name="isComplete")
    val isComplete: Boolean?,

    @param:Json(name="lastListenTime")
    val lastListenTime: Date?,

    @param:Json(name="duration")
    val duration: Int?
)
