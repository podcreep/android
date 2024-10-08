package com.podcreep.mobile.ui.library

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun SubscribedPodcasts(viewModel : SubscribedPodcastsViewModel = hiltViewModel()) {
  val episodes = viewModel.episodes.collectAsState(initial = emptyList())

  LazyColumn {
    itemsIndexed(episodes.value) { index, sub ->
      Text(sub.podcast?.title ?: "")
    }
  }
}
