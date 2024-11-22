package com.podcreep.mobile.ui.library

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.toRoute
import com.podcreep.mobile.data.SubscriptionsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EpisodeDetailsViewModel @Inject constructor(
  savedStateHandle: SavedStateHandle,
  repo : SubscriptionsRepository)
  : ViewModel() {

  val route = savedStateHandle.toRoute<NavItem.EpisodeDetails>()
  val episode = repo.episode(route.podcastID, route.episodeID)
  val podcast = repo.podcast(route.podcastID)
}
