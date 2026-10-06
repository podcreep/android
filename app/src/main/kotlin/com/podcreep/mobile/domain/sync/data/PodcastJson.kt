package com.podcreep.mobile.domain.sync.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PodcastJson(
    @param:Json(name="id")
    var id: Long,

    @param:Json(name="title")
    var title: String,

    @param:Json(name="description")
    var description: String,

    @param:Json(name="imageUrl")
    var imageUrl: String,

    @param:Json(name="episodes")
    val episodes: List<EpisodeJson>?,

    @param:Json(name="isSubscribed")
    val isSubscribed: Boolean?)

