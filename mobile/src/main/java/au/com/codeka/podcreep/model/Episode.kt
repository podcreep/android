package au.com.codeka.podcreep.model

data class Episode(
    val id: Long,
    val title: String,
    val description: String,
    val mediaUrl: String,
    val pubDate: String)
