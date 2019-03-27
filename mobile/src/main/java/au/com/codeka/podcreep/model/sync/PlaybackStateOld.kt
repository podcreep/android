package au.com.codeka.podcreep.model.sync

data class PlaybackStateOld(
    val podcastID: Long,
    val episodeID: Long,
    val position: Int)
