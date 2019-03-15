package au.com.codeka.podcreep.app.podcasts.subscriptions

import android.view.View
import android.view.ViewGroup
import au.com.codeka.podcreep.app.podcasts.details.DetailsScreen
import au.com.codeka.podcreep.model.Podcast
import au.com.codeka.podcreep.model.store.Store
import au.com.codeka.podcreep.ui.Screen
import au.com.codeka.podcreep.ui.ScreenContext
import au.com.codeka.podcreep.ui.ScreenOptions

class SubscriptionsScreen(private val store: Store): Screen() {
  private var layout: SubscriptionsLayout? = null

  override val options: ScreenOptions
    get() = ScreenOptions(isRootScreen = true)

  override fun onCreate(context: ScreenContext, container: ViewGroup) {
    super.onCreate(context, container)

    layout = SubscriptionsLayout(
        context.activity,
        this,
        store.subscriptions(),
        object : SubscriptionsLayout.Callbacks {
          override fun onViewPodcastClick(podcast: Podcast) {
            context.pushScreen<DetailsScreen>(podcast)
          }
        })
  }

  override fun onShow(): View? {
    super.onShow()
    return layout
  }
}

