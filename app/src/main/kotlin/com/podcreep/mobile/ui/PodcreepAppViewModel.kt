package com.podcreep.mobile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.Player
import com.podcreep.mobile.domain.AuthUseCase
import com.podcreep.mobile.service.MediaServiceClient
import com.podcreep.mobile.service.SyncManager
import com.podcreep.mobile.util.L
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PodcreepAppViewModel @Inject constructor(
  private val auth: AuthUseCase,
  private val mediaServiceClient: MediaServiceClient,
  private val syncManager: SyncManager) : ViewModel() {
  private val L = L("PodcreepAppViewModel")

  val isLoggedIn
    get() = auth.isLoggedIn

  fun logout() {
    viewModelScope.launch {
      auth.logout()
    }
  }

  /** Called to maybe trigger a sync. Used when we first load. */
  fun maybeSync() {
    syncManager.maybeSync()
  }

  // This is a flow that is just true when we should show the bottom sheet vs. when we shouldn't.
  val hideBottomSheet = callbackFlow {
    val callbacks = mediaServiceClient.addCallback(object : MediaServiceClient.Callbacks() {
      override fun onPlaybackStateChanged(isPlaying: Boolean, playbackState: Int) {
        L.info("isPlaying = $isPlaying, state = $playbackState")
        val shouldHide = when {
          isPlaying -> false
          playbackState == Player.STATE_BUFFERING -> false
          playbackState == Player.STATE_READY -> false
          else -> true
        }

        trySend(shouldHide)
      }
    })

    awaitClose { mediaServiceClient.removeCallback(callbacks) }
  }
}
