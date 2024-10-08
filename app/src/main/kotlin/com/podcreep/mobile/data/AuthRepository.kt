package com.podcreep.mobile.data

import javax.inject.Inject

class AuthRepository @Inject constructor(private val dataSource: AuthRemoteDataSource) {
  /**
   * Attempts to log in with the given username/password and returns the cookie if successful.
   */
  suspend fun login(username: String, password: String): String {
    return dataSource.login(username, password)
  }
}
