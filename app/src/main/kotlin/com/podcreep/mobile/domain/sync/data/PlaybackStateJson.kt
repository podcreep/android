package com.podcreep.mobile.domain.sync.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.Date

@JsonClass(generateAdapter = false)
data class PlaybackStateJson(
    @Json(name="podcastID")
    val podcastID: Long,

    @Json(name="episodeID")
    val episodeID: Long,

    @Json(name="position")
    val position: Int,

    @Json(name="lastUpdated")
    val lastUpdated: Date
)
