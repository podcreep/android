package au.com.codeka.podcreep.app.podcasts.details

import android.view.View
import android.view.ViewGroup
import au.com.codeka.podcreep.concurrency.TaskRunner
import au.com.codeka.podcreep.concurrency.Threads
import au.com.codeka.podcreep.model.Podcast
import au.com.codeka.podcreep.model.PodcastList
import au.com.codeka.podcreep.net.HttpRequest
import au.com.codeka.podcreep.net.Server
import au.com.codeka.podcreep.ui.Screen
import au.com.codeka.podcreep.ui.ScreenContext

class DetailsScreen(
    private val taskRunner: TaskRunner,
    private val podcast: Podcast)
  : Screen() {

  private var layout: DetailsLayout? = null

  override fun onCreate(context: ScreenContext, container: ViewGroup) {
    super.onCreate(context, container)

    layout = DetailsLayout(context.activity, podcast, taskRunner, object : DetailsLayout.Callbacks {
      override fun onFoo() {
        //context.pushScreen<DetailsScreen>(podcast)
      }
    })

    taskRunner.runTask({
      val request = Server.request("/api/podcasts/" + podcast.id)
          .method(HttpRequest.Method.GET)
          .build()
      var resp = request.execute<PodcastList>()
      taskRunner.runTask({
        //adapter = TrendingTabLayout.Adapter(resp.podcasts, callbacks)
        // TODO: send it to the layout..
      }, Threads.UI)
    }, Threads.BACKGROUND)
  }

  override fun onShow(): View? {
    return layout
  }
}
