package com.podcreep.mobile.ui

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
      override fun onPlaybackStateChanged(state: PlaybackStateCompat) {
        L.info("state = %d", state.state)
        val shouldHide = when (state.state) {
          PlaybackStateCompat.STATE_PLAYING -> false
          PlaybackStateCompat.STATE_PAUSED -> false
          PlaybackStateCompat.STATE_BUFFERING -> false
          else -> true
        }

        trySend(shouldHide)
      }
    })

    awaitClose { mediaServiceClient.removeCallback(callbacks) }
  }
}
