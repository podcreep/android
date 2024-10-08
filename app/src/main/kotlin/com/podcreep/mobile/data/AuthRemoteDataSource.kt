package com.podcreep.mobile.data

import com.podcreep.mobile.util.Server
import com.podcreep.mobile.util.await
import com.podcreep.mobile.util.fromJson
import com.podcreep.mobile.util.toRequestBody
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import javax.inject.Inject

@JsonClass(generateAdapter = false)
data class LoginRequest(
  @Json(name="username")
  val username: String,

  @Json(name="password")
  val password: String
)

@JsonClass(generateAdapter = false)
data class LoginResponse(
  @Json(name="cookie")
  val cookie: String
)

class AuthException(msg: String): Exception(msg) {}

class AuthRemoteDataSource @Inject constructor(private val server: Server) {
  /**
   * Attempts to log in with the given username/password and returns the cookie if successful.
   */
  suspend fun login(username: String, password: String): String {
    val request = server.request("/api/accounts/login")
      .post(LoginRequest(username, password).toRequestBody())
    val resp = server.call(request).await()
    if (resp.code == 401) {
      // TODO: these should be translatable.
      throw AuthException("Invalid username/password")
    }
    if (resp.code != 200) {
      // TODO: these should be translatable.
      throw AuthException("Unable to login at this time, please try again later")
    }
    val loginResp = server.call(request).await().fromJson<LoginResponse>()
    return loginResp.cookie
  }
}
