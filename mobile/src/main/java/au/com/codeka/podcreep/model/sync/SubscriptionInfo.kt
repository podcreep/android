package au.com.codeka.podcreep.model.sync

data class SubscriptionInfo(
    val id: Long,
    val podcastID: Long,
    var podcast: PodcastInfo?,
    val positions: Map<Long, Int>)
