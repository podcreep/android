package com.podcreep.mobile.ui.library

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@Composable
fun NewReleases(onEpisodeDetailsClick: (podcastID: Long, episodeID: Long) -> Unit, viewModel : NewReleasesViewModel = hiltViewModel()) {
  val episodeWithPodcast = viewModel.episodeWithPodcast.collectAsState(initial = emptyList())

  LazyColumn {
    itemsIndexed(episodeWithPodcast.value) { index, epAndPod ->
      EpisodeListEntry(epAndPod.podcast, epAndPod.episode, onEpisodeDetailsClick)
    }
  }
}
