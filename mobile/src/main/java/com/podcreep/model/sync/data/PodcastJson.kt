package com.podcreep.model.sync.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = false)
data class PodcastJson(
    @Json(name="id")
    var id: Long,

    @Json(name="title")
    var title: String,

    @Json(name="description")
    var description: String,

    @Json(name="imageUrl")
    var imageUrl: String,

    @Json(name="episodes")
    val episodes: List<EpisodeJson>?,

    @Json(name="isSubscribed")
    val isSubscribed: Boolean?)

