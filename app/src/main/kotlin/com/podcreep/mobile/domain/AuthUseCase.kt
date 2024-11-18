package com.podcreep.mobile.domain

import com.podcreep.mobile.Settings
import com.podcreep.mobile.data.AuthRepository
import com.podcreep.mobile.service.SyncManager
import com.podcreep.mobile.util.Server
import javax.inject.Inject

class AuthUseCase @Inject constructor(
  private val server: Server,
  private val settings: Settings,
  private val authRepository: AuthRepository,
  private val syncManager: SyncManager) {

  suspend fun login(username: String, password: String) {
    val cookie = authRepository.login(username, password)
    server.updateCookie(cookie)
    settings.put(Settings.COOKIE, cookie)
  }

  suspend fun logout() {
    server.updateCookie("")
    settings.put(Settings.COOKIE, "")

    syncManager.logout()
  }

  val isLoggedIn
    get() = server.isLoggedIn
}
