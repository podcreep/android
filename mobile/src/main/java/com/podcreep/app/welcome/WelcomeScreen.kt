package com.podcreep.app.welcome

import android.view.View
import android.view.ViewGroup
import com.podcreep.R
import com.podcreep.ui.Screen
import com.podcreep.ui.ScreenContext
import com.podcreep.ui.ScreenOptions
import com.podcreep.ui.SharedViews

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
        context.pushScreen<LoginScreen>(SharedViews.builder().addSharedView(R.id.logo).build())
      }

      override fun onRegisterClick() {

      }
    })
  }

  override fun onShow(): View? {
    super.onShow()
    return layout
  }
}
