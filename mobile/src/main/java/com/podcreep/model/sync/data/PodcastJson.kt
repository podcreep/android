package com.podcreep.model.sync.data

import com.squareup.moshi.Json

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

    @Json(name="subscription")
    val subscription: SubscriptionJson?)

