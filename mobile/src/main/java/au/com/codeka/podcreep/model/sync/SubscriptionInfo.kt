package au.com.codeka.podcreep.model.sync

data class SubscriptionInfo(
    val id: Long,
    val podcastID: Long,
    var podcast: PodcastInfo?,
    val oldestUnlistenedEpisodeID: Long,
    val positions: Map<Long, Int>)
