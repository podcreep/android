package com.podcreep.mobile.ui

import androidx.lifecycle.ViewModel
import com.podcreep.mobile.domain.AuthUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PodcreepAppViewModel @Inject constructor(
  private val auth: AuthUseCase) : ViewModel() {

  val isLoggedIn
    get() = auth.isLoggedIn
}
