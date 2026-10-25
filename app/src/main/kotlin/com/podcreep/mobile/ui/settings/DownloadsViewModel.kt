package com.podcreep.mobile.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.podcreep.mobile.data.SubscriptionsRepository
import com.podcreep.mobile.domain.cache.EpisodeMediaCache
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.util.Date
import javax.inject.Inject

data class DownloadedEpisodeItem(
  val podcastId: Long,
  val episodeId: Long,
  val file: File,
  val sizeBytes: Long,
  val lastModified: Long,
  val pubDate: Date?,
  val downloadTime: Date?,
  val episodeTitle: String,
  val podcastTitle: String
)

data class DownloadsUiState(
  val episodes: List<DownloadedEpisodeItem> = emptyList(),
  val totalSizeBytes: Long = 0L,
  val downloadCount: Int = 0
)

@HiltViewModel
class DownloadsViewModel @Inject constructor(
  private val mediaCache: EpisodeMediaCache,
  private val subscriptionsRepository: SubscriptionsRepository
) : ViewModel() {

  private val _refreshTrigger = MutableStateFlow(0)

  val uiState: StateFlow<DownloadsUiState> = combine(
    subscriptionsRepository.podcasts(),
    subscriptionsRepository.allEpisodes(),
    _refreshTrigger
  ) { podcasts, episodes, _ ->
    val podcastMap = podcasts.associateBy { it.id }
    val episodeMap = episodes.associateBy { it.id }

    val files = mediaCache.getDownloadedFiles()
    val items = files.map { fileInfo ->
      val ep = episodeMap[fileInfo.episodeId]
      val pod = podcastMap[fileInfo.podcastId] ?: ep?.let { podcastMap[it.podcastID] }

      DownloadedEpisodeItem(
        podcastId = fileInfo.podcastId,
        episodeId = fileInfo.episodeId,
        file = fileInfo.file,
        sizeBytes = fileInfo.sizeBytes,
        lastModified = fileInfo.lastModified,
        pubDate = ep?.pubDate,
        downloadTime = ep?.downloadTime ?: Date(fileInfo.lastModified),
        episodeTitle = ep?.title ?: "Episode ${fileInfo.episodeId}",
        podcastTitle = pod?.title ?: "Podcast ${fileInfo.podcastId}"
      )
    }.sortedWith(
      compareByDescending<DownloadedEpisodeItem> { it.downloadTime?.time ?: it.lastModified }
        .thenByDescending { it.lastModified }
    )

    val totalSize = items.sumOf { it.sizeBytes }
    DownloadsUiState(
      episodes = items,
      totalSizeBytes = totalSize,
      downloadCount = items.size
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = DownloadsUiState()
  )

  init {
    viewModelScope.launch {
      mediaCache.statusUpdates.collect {
        _refreshTrigger.value = _refreshTrigger.value + 1
      }
    }
  }

  fun cleanUpDownloads(targetBytesToFree: Long) {
    viewModelScope.launch(Dispatchers.IO) {
      val currentEpisodes = uiState.value.episodes
      if (currentEpisodes.isEmpty() || targetBytesToFree <= 0) return@launch

      val sortedOldestFirst = currentEpisodes.sortedWith(
        compareBy<DownloadedEpisodeItem> { it.downloadTime?.time ?: it.lastModified }
          .thenBy { it.lastModified }
      )

      var freedBytes = 0L
      for (item in sortedOldestFirst) {
        if (freedBytes + item.sizeBytes <= targetBytesToFree) {
          val deleted = mediaCache.deleteDownload(item.podcastId, item.episodeId)
          if (deleted) {
            freedBytes += item.sizeBytes
          }
        } else {
          break
        }
      }

      _refreshTrigger.value = _refreshTrigger.value + 1
    }
  }

  fun deleteSingleDownload(podcastId: Long, episodeId: Long) {
    viewModelScope.launch(Dispatchers.IO) {
      mediaCache.deleteDownload(podcastId, episodeId)
      _refreshTrigger.value = _refreshTrigger.value + 1
    }
  }
}
