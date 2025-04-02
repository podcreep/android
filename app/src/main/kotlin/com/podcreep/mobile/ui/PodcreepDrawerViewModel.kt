package com.podcreep.mobile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.podcreep.mobile.domain.AuthUseCase
import com.podcreep.mobile.util.L
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PodcreepDrawerViewModel @Inject constructor(
    private val auth: AuthUseCase
) : ViewModel() {
  private val L = L("PodcreepDrawerViewModel")

  val isLoggedIn
      get() = auth.isLoggedIn

  fun logout() {
    viewModelScope.launch {
      auth.logout()
    }
  }
}
