package au.com.codeka.podcreep.app.welcome

import android.view.View
import au.com.codeka.podcreep.ui.Screen
import android.view.ViewGroup
import au.com.codeka.podcreep.ui.ScreenContext
import au.com.codeka.podcreep.ui.ScreenOptions

/**
 * The WelcomeScreen shows the login/register button when you haven't previously logged in.
 */
class WelcomeScreen: Screen() {
  private var layout: WelcomeLayout? = null

  override val options: ScreenOptions
    get() = ScreenOptions(enableActionBar = false)

  override fun onCreate(context: ScreenContext, container: ViewGroup) {
    super.onCreate(context, container)

    layout = WelcomeLayout(context.activity, callbacks = object : WelcomeLayout.Callbacks {
      override fun onLoginClick() {
        context.pushScreen<LoginScreen>()
      }

      override fun onRegisterClick() {

      }
    })
  }

  override fun onShow(): View? {
    return layout
  }
}
