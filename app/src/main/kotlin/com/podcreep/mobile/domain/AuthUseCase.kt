package com.podcreep.mobile.domain

import com.podcreep.mobile.Settings
import com.podcreep.mobile.data.AuthRepository
import com.podcreep.mobile.util.Server
import javax.inject.Inject

class AuthUseCase @Inject constructor(
  private val server: Server,
  private val settings: Settings,
  private val authRepository: AuthRepository) {

  suspend fun login(username: String, password: String) {
    val cookie = authRepository.login(username, password)
    server.updateCookie(cookie)
    settings.put(Settings.COOKIE, cookie)
  }

  val isLoggedIn
    get() = server.isLoggedIn
}
