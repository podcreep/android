package com.podcreep.mobile.domain.sync.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.Date

@JsonClass(generateAdapter = true)
data class PlaybackStateJson(
    @param:Json(name="podcastID")
    val podcastID: Long,

    @param:Json(name="episodeID")
    val episodeID: Long,

    @param:Json(name="position")
    val position: Int,

    @param:Json(name="lastUpdated")
    val lastUpdated: Date
)
