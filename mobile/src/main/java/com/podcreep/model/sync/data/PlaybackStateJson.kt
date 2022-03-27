package com.podcreep.model.sync.data

import com.squareup.moshi.Json

data class PlaybackStateJson(
    @Json(name="podcastID")
    val podcastID: Long,

    @Json(name="episodeID")
    val episodeID: Long,

    @Json(name="position")
    val position: Int
)
