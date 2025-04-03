package com.podcreep.mobile.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.podcreep.mobile.data.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
  private val settingsRepository: SettingsRepository
) : ViewModel() {
  private val volumeBoost_: MutableStateFlow<Long> = MutableStateFlow(0)
  var volumeBoost = volumeBoost_.asStateFlow()

  fun setVolumeBoost(value: Long) {
    volumeBoost_.value = value
    viewModelScope.launch {
      settingsRepository.setValue("VolumeBoost", value)
    }
  }

  init {
    viewModelScope.launch {
      volumeBoost_.value = settingsRepository.getInt("VolumeBoost", 0)
    }
  }
}