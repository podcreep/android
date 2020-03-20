package au.com.codeka.podcreep.model.sync

data class PlaybackState(
    val podcastID: Long,
    val episodeID: Long,
    val position: Int)
