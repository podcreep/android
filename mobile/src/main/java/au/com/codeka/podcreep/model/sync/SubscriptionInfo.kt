package au.com.codeka.podcreep.model.sync

data class SubscriptionInfo(
    var podcast: PodcastInfo,
    val positions: Map<Long, Int>)
