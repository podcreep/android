package au.com.codeka.podcreep.app.podcasts

import au.com.codeka.podcreep.ui.Screen
import au.com.codeka.podcreep.ui.ScreenOptions

class PodcastsScreen: Screen() {
  override val options: ScreenOptions
    get() = ScreenOptions(isRootScreen = true)
}
