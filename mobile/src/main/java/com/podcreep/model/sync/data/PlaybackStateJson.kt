package com.podcreep.model.sync.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = false)
data class PlaybackStateJson(
    @Json(name="podcastID")
    val podcastID: Long,

    @Json(name="episodeID")
    val episodeID: Long,

    @Json(name="position")
    val position: Int
)
