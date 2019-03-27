package au.com.codeka.podcreep.model.sync

data class EpisodeOld(
    val id: Long,
    val podcastID: Long,
    val title: String,
    val description: String,
    val mediaUrl: String,
    val pubDate: String)
