package com.podcreep.model.sync.data

import com.squareup.moshi.Json

data class PodcastListJson(
    @Json(name="podcasts")
    val podcasts: List<PodcastJson>)
