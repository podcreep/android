package com.podcreep.model.sync.data

import com.squareup.moshi.Json

data class SubscriptionJson(
    @Json(name="podcast")
    var podcast: PodcastJson,

    @Json(name="positions")
    val positions: Map<Long, Int>)