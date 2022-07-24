package com.podcreep.app.welcome

import android.view.View
import android.view.ViewGroup
import com.podcreep.Settings
import com.podcreep.app.podcasts.discover.DiscoverScreen
import com.podcreep.concurrency.TaskRunner
import com.podcreep.concurrency.Threads
import com.podcreep.net.HttpException
import com.podcreep.net.HttpRequest
import com.podcreep.net.Server
import com.podcreep.ui.Screen
import com.podcreep.ui.ScreenContext
import com.podcreep.ui.ScreenOptions
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

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

class LoginScreen(val taskRunner: TaskRunner): Screen() {
  private var layout: LoginLayout? = null

  override val options: ScreenOptions
    get() = ScreenOptions(enableActionBar = false)

  override fun onCreate(context: ScreenContext, container: ViewGroup) {
    super.onCreate(context, container)

    layout = LoginLayout(context.activity, callbacks = object : LoginLayout.Callbacks {
      override fun onSignIn(username: String, password: String) {
        taskRunner.runTask(Threads.UI, { httpError: HttpException ->
          layout!!.showError(httpError.message ?: "Unknown error occured")
        }) {
          val request = Server.request("/api/accounts/login")
              .method(HttpRequest.Method.POST)
              .body(LoginRequest(username, password))
              .build()
          val resp = request.execute<LoginResponse>()
          val s = Settings(context.activity)
          s.put(Settings.COOKIE, resp.cookie)
          Server.updateCookie(resp.cookie)

          taskRunner.runTask(Threads.UI) {
            context.pushScreen<DiscoverScreen>()
          }
        }
      }
    })
  }

  override fun onShow(): View? {
    super.onShow()
    return layout
  }
}
