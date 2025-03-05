package com.podcreep.mobile.ui.library

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.podcreep.mobile.data.SubscriptionsRepository
import com.podcreep.mobile.service.MediaServiceClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EpisodeDetailsViewModel @Inject constructor(
  savedStateHandle: SavedStateHandle,
  private val mediaServiceClient: MediaServiceClient,
  repo : SubscriptionsRepository)
  : ViewModel() {

  val route = savedStateHandle.toRoute<NavItem.EpisodeDetails>()
  val episode = repo.episode(route.podcastID, route.episodeID)
  val podcast = repo.podcast(route.podcastID)

  fun play() {
    viewModelScope.launch {
      mediaServiceClient.play(podcast.first(), episode.first())
    }
  }
}
