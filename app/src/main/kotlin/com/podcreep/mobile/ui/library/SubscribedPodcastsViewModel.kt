package com.podcreep.mobile.ui.library

import androidx.lifecycle.ViewModel
import com.podcreep.mobile.data.SubscriptionsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SubscribedPodcastsViewModel @Inject constructor(private val repo : SubscriptionsRepository)
  : ViewModel() {

  val subscriptions = repo.subscriptions()
}
