package com.podcreep.mobile.domain.sync.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PodcastListJson(
    @param:Json(name="podcasts")
    val podcasts: List<PodcastJson>)
