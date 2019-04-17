package au.com.codeka.podcreep.model.sync

data class PodcastInfo(
    var id: Long,
    var title: String,
    var description: String,
    var imageUrl: String,
    val episodes: List<EpisodeOld>?,
    val subscription: SubscriptionInfo?)

