package au.com.codeka.podcreep.app.podcasts.discover

import android.view.View
import android.view.ViewGroup
import au.com.codeka.podcreep.app.podcasts.details.DetailsScreen
import au.com.codeka.podcreep.concurrency.TaskRunner
import au.com.codeka.podcreep.model.sync.PodcastInfo
import au.com.codeka.podcreep.ui.Screen
import au.com.codeka.podcreep.ui.ScreenContext
import au.com.codeka.podcreep.ui.ScreenOptions

class DiscoverScreen(private val taskRunner: TaskRunner): Screen() {
  private var layout: DiscoverLayout? = null

  override val options: ScreenOptions
    get() = ScreenOptions(isRootScreen = true)

  override fun onCreate(context: ScreenContext, container: ViewGroup) {
    super.onCreate(context, container)

    layout = DiscoverLayout(context.activity, taskRunner, object : DiscoverLayout.Callbacks {
      override fun onViewPodcastClick(podcast: PodcastInfo) {
        context.pushScreen<DetailsScreen>(podcast)
      }
    })
  }

  override fun onShow(): View? {
    super.onShow()
    return layout
  }
}
