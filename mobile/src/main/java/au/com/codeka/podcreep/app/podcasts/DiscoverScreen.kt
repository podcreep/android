package au.com.codeka.podcreep.app.podcasts

import android.view.View
import android.view.ViewGroup
import au.com.codeka.podcreep.ui.Screen
import au.com.codeka.podcreep.ui.ScreenContext
import au.com.codeka.podcreep.ui.ScreenOptions

class DiscoverScreen: Screen() {
  private var layout: DiscoverLayout? = null

  override val options: ScreenOptions
    get() = ScreenOptions(isRootScreen = true)

  override fun onCreate(context: ScreenContext, container: ViewGroup) {
    super.onCreate(context, container)

    layout = DiscoverLayout(context.activity, object: DiscoverLayout.Callbacks {
      override fun onFoo() {
        TODO("not implemented")
      }
    })
  }

  override fun onShow(): View? {
    return layout
  }
}
