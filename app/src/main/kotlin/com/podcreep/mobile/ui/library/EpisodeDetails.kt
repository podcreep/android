package com.podcreep.mobile.ui.library

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun EpisodeDetails(viewModel: EpisodeDetailsViewModel = hiltViewModel()) {
  val episode = viewModel.episode.collectAsState(initial = null)
  val podcast = viewModel.podcast.collectAsState(initial = null)

  Text(
    text = episode.value?.title ?: ""
  )
  Text(
    text = podcast.value?.title ?: ""
  )
}
