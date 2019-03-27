package au.com.codeka.podcreep.app.podcasts.details

import android.view.View
import android.view.ViewGroup
import au.com.codeka.podcreep.app.service.MediaServiceClient
import au.com.codeka.podcreep.concurrency.TaskRunner
import au.com.codeka.podcreep.concurrency.Threads
import au.com.codeka.podcreep.model.sync.EpisodeOld
import au.com.codeka.podcreep.model.sync.PodcastOld
import au.com.codeka.podcreep.net.HttpRequest
import au.com.codeka.podcreep.net.Server
import au.com.codeka.podcreep.ui.Screen
import au.com.codeka.podcreep.ui.ScreenContext

class DetailsScreen(
    private val taskRunner: TaskRunner,
    private val podcast: PodcastOld)
  : Screen() {

  private var layout: DetailsLayout? = null

  override fun onCreate(context: ScreenContext, container: ViewGroup) {
    super.onCreate(context, container)

    layout = DetailsLayout(context.activity, podcast, taskRunner, object : DetailsLayout.Callbacks {
      override fun onEpisodePlay(podcast: PodcastOld, episode: EpisodeOld) {
        MediaServiceClient.i.play(podcast, episode)
      }
    })

    taskRunner.runTask({
      val request = Server.request("/api/podcasts/" + podcast.id)
          .method(HttpRequest.Method.GET)
          .build()
      var podcast = request.execute<PodcastOld>()
      taskRunner.runTask({
        layout?.refresh(podcast)
      }, Threads.UI)
    }, Threads.BACKGROUND)
  }

  override fun onShow(): View? {
    super.onShow()
    return layout
  }
}
