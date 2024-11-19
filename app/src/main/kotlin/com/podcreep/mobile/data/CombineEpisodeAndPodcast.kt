package com.podcreep.mobile.data

import com.podcreep.mobile.data.local.Episode
import com.podcreep.mobile.data.local.Podcast
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class EpisodeWithPodcast(val episode: Episode, val podcast: Podcast)

fun Flow<List<Episode>>.combineWithPodcasts(podcasts: Flow<List<Podcast>>): Flow<List<EpisodeWithPodcast>> {
  return combine(podcasts) { eps, pods ->
    val combination = ArrayList<EpisodeWithPodcast>()
    for (ep in eps) {
      val podcast = pods.find { it.id == ep.podcastID }
      if (podcast == null) {
        // TODO: make some kind of default?
        continue
      }
      combination.add(EpisodeWithPodcast(ep, podcast))
    }
    combination
  }
}
