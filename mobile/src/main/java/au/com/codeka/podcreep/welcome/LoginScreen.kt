package au.com.codeka.podcreep.welcome

import android.view.View
import android.view.ViewGroup
import au.com.codeka.podcreep.app.podcasts.DiscoverScreen
import au.com.codeka.podcreep.concurrency.TaskRunner
import au.com.codeka.podcreep.concurrency.Threads
import au.com.codeka.podcreep.net.HttpRequest
import au.com.codeka.podcreep.net.Server
import au.com.codeka.podcreep.ui.Screen
import au.com.codeka.podcreep.ui.ScreenContext
import au.com.codeka.podcreep.ui.ScreenOptions

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val cookie: String
)

class LoginScreen(val taskRunner: TaskRunner): Screen() {
  private var layout: LoginLayout? = null

  override val options: ScreenOptions
    get() = ScreenOptions(enableActionBar = false)

  override fun onCreate(context: ScreenContext, container: ViewGroup) {
    super.onCreate(context, container)

    layout = LoginLayout(context.activity, callbacks = object: LoginLayout.Callbacks {
      override fun onSignIn(username: String, password: String) {
        taskRunner.runTask({
          val request = Server.request("/api/accounts/login")
              .method(HttpRequest.Method.POST)
              .body(LoginRequest(username, password))
              .build()
          val resp = request.execute<LoginResponse>()
          Server.updateCookie(resp.cookie)

          taskRunner.runTask({
            context.pushScreen<DiscoverScreen>()
          }, Threads.UI)
        }, Threads.BACKGROUND)
      }
    })
  }

  override fun onShow(): View? {
    return layout
  }
}
