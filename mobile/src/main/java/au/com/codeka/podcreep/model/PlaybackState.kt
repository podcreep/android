package au.com.codeka.podcreep.model

data class PlaybackState(
    val podcastID: Long,
    val episodeID: Long,
    val position: Int)
