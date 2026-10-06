package com.podcreep.mobile.ui.library

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.podcreep.mobile.data.SubscriptionsRepository
import com.podcreep.mobile.domain.cache.EpisodeMediaCache
import com.podcreep.mobile.service.MediaServiceClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EpisodeDetailsViewModel @Inject constructor(
  savedStateHandle: SavedStateHandle,
  private val mediaServiceClient: MediaServiceClient,
  private val mediaCache: EpisodeMediaCache,
  repo: SubscriptionsRepository
) : ViewModel() {

  val route = savedStateHandle.toRoute<NavItem.EpisodeDetails>()
  val episode = repo.episode(route.podcastID, route.episodeID)
  val podcast = repo.podcast(route.podcastID)

  @OptIn(ExperimentalCoroutinesApi::class)
  val downloadProgress: Flow<EpisodeMediaCache.DownloadProgress> = combine(podcast, episode) { p, e ->
    Pair(p, e)
  }.flatMapLatest { (p, e) ->
    mediaCache.observeProgress(p, e)
  }

  fun play() {
    viewModelScope.launch {
      val p = podcast.first()
      val e = episode.first()
      mediaServiceClient.play(p, e)
    }
  }

  fun download() {
    viewModelScope.launch {
      val p = podcast.first()
      val e = episode.first()
      mediaCache.queueDownload(p, e)
    }
  }
}
