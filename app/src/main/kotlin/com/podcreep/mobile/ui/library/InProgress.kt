package com.podcreep.mobile.ui.library

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun InProgress(onPodcastDetailsClick: (podcastID: Long, episodeID: Long) -> Unit, viewModel : InProgressViewModel = hiltViewModel()) {
  val episodeWithPodcast = viewModel.episodeWithPodcast.collectAsState(initial = emptyList())

  LazyColumn {
    itemsIndexed(episodeWithPodcast.value) { index, epAndPod ->
      EpisodeListEntry(epAndPod.podcast, epAndPod.episode, onPodcastDetailsClick)
    }
  }
}
