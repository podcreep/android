package au.com.codeka.podcreep.model

data class Podcast(
    val title: String,
    val description: String,
    val imageUrl: String)

data class PodcastList(
    val podcasts: Array<Podcast>)
