package au.com.codeka.podcreep.app.podcasts.subscriptions

import android.view.View
import android.view.ViewGroup
import au.com.codeka.podcreep.app.podcasts.details.DetailsScreen
import au.com.codeka.podcreep.app.podcasts.discover.TrendingTabLayout
import au.com.codeka.podcreep.concurrency.TaskRunner
import au.com.codeka.podcreep.concurrency.Threads
import au.com.codeka.podcreep.model.Podcast
import au.com.codeka.podcreep.model.PodcastList
import au.com.codeka.podcreep.model.SubscriptionList
import au.com.codeka.podcreep.net.HttpRequest
import au.com.codeka.podcreep.net.Server
import au.com.codeka.podcreep.ui.Screen
import au.com.codeka.podcreep.ui.ScreenContext
import au.com.codeka.podcreep.ui.ScreenOptions

class SubscriptionsScreen(private val taskRunner: TaskRunner): Screen() {
  private var layout: SubscriptionsLayout? = null

  override val options: ScreenOptions
    get() = ScreenOptions(isRootScreen = true)

  override fun onCreate(context: ScreenContext, container: ViewGroup) {
    super.onCreate(context, container)

    layout = SubscriptionsLayout(context.activity, taskRunner, object : SubscriptionsLayout.Callbacks {
      override fun onViewPodcastClick(podcast: Podcast) {
        context.pushScreen<DetailsScreen>(podcast)
      }
    })
  }

  override fun onShow(): View? {
    taskRunner.runTask({
      val request = Server.request("/api/subscriptions")
          .method(HttpRequest.Method.GET)
          .build()
      var resp = request.execute<SubscriptionList>()
      taskRunner.runTask({
        layout?.refresh(resp.subscriptions)
      }, Threads.UI)
    }, Threads.BACKGROUND)

    return layout
  }
}

