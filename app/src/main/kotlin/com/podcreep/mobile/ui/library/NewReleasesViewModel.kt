package com.podcreep.mobile.ui.library

import androidx.lifecycle.ViewModel
import com.podcreep.mobile.data.SubscriptionsRepository
import com.podcreep.mobile.data.combineWithPodcasts
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NewReleasesViewModel @Inject constructor(private val repo : SubscriptionsRepository)
  : ViewModel() {

  val episodes = repo.newEpisodes()
  val episodeWithPodcast = episodes.combineWithPodcasts(repo.podcasts())
}
