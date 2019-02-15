package au.com.codeka.podcreep.app.podcasts.details

import android.content.Intent
import android.util.Log
import android.view.View
import android.view.ViewGroup
import au.com.codeka.podcreep.app.service.MediaService
import au.com.codeka.podcreep.app.service.MediaServiceClient
import au.com.codeka.podcreep.concurrency.TaskRunner
import au.com.codeka.podcreep.concurrency.Threads
import au.com.codeka.podcreep.model.Episode
import au.com.codeka.podcreep.model.Podcast
import au.com.codeka.podcreep.net.HttpRequest
import au.com.codeka.podcreep.net.Server
import au.com.codeka.podcreep.ui.Screen
import au.com.codeka.podcreep.ui.ScreenContext
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi

class DetailsScreen(
    private val taskRunner: TaskRunner,
    private val podcast: Podcast)
  : Screen() {

  private var layout: DetailsLayout? = null

  override fun onCreate(context: ScreenContext, container: ViewGroup) {
    super.onCreate(context, container)

    layout = DetailsLayout(context.activity, podcast, taskRunner, object : DetailsLayout.Callbacks {
      override fun onEpisodePlay(podcast: Podcast, episode: Episode) {
        MediaServiceClient.i.play(podcast, episode)
      }
    })

    taskRunner.runTask({
      val request = Server.request("/api/podcasts/" + podcast.id)
          .method(HttpRequest.Method.GET)
          .build()
      var podcast = request.execute<Podcast>()
      taskRunner.runTask({
        layout?.refresh(podcast)
      }, Threads.UI)
    }, Threads.BACKGROUND)
  }

  override fun onShow(): View? {
    return layout
  }
}
