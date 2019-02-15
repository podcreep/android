package au.com.codeka.podcreep.model

data class Podcast(
    val id: Long,
    val title: String,
    val description: String,
    val imageUrl: String,
    val episodes: List<Episode>?,
    val subscription: Subscription?)
