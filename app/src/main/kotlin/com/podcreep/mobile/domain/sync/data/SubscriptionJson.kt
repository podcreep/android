package com.podcreep.mobile.domain.sync.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = false)
data class SubscriptionJson(
    @Json(name="podcast")
    var podcast: PodcastJson)
