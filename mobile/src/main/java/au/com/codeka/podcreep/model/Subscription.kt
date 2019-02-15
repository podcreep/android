package au.com.codeka.podcreep.model

data class Subscription(
    val id: Long,
    val podcastID: Long,
    val podcast: Podcast?,
    val oldestUnlistenedEpisodeID: Long,
    val positions: Map<Long, Int>)
