package com.podcreep.mobile.ui.auth

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.podcreep.mobile.Settings
import com.podcreep.mobile.domain.AuthUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor (
  private val auth: AuthUseCase,
  private val settings: Settings) : ViewModel() {

  var username by mutableStateOf("")
    private set
  fun setUsernameValue(value: String) {
    username = value

    // If you've typed a new username or password, clear the error.
    isError = false
    errorMessage = ""
  }

  var password by mutableStateOf("")
    private set
  fun setPasswordValue(value: String) {
    password = value

    // If you've typed a new username or password, clear the error.
    isError = false
    errorMessage = ""
  }

  var isError by mutableStateOf(false)
    private set
  var errorMessage by mutableStateOf("")
    private set

  fun login() {
    Log.i("DEANH", "logging in $username $password")
    viewModelScope.launch {
      try {
        auth.login(username, password)
        Log.i("DEANH", "login successful: ${settings.getString(Settings.COOKIE)}")
      } catch (e: Exception) {
        isError = true
        errorMessage = e.message ?: "An unknown error occurred."
      }
    }
  }
}
