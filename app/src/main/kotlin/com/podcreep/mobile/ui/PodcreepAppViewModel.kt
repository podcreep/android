package com.podcreep.mobile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.podcreep.mobile.domain.AuthUseCase
import com.podcreep.mobile.service.SyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PodcreepAppViewModel @Inject constructor(
  private val auth: AuthUseCase, private val syncManager: SyncManager) : ViewModel() {

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
}
