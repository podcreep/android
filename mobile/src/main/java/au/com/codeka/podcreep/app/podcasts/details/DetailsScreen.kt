package au.com.codeka.podcreep.app.podcasts.details

import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import au.com.codeka.podcreep.app.podcasts.episode.EpisodeDetailsScreen
import au.com.codeka.podcreep.app.service.MediaServiceClient
import au.com.codeka.podcreep.concurrency.TaskRunner
import au.com.codeka.podcreep.model.store.Episode
import au.com.codeka.podcreep.model.store.Podcast
import au.com.codeka.podcreep.model.store.Store
import au.com.codeka.podcreep.ui.Screen
import au.com.codeka.podcreep.ui.ScreenContext

class DetailsScreen(
      private val taskRunner: TaskRunner,
      private val store: Store,
      private val podcastID: Long,
      private var podcast: LiveData<Podcast>)
    : Screen() {

  private var layout: DetailsLayout? = null
  private lateinit var context: ScreenContext

  override fun onCreate(context: ScreenContext, container: ViewGroup) {
    super.onCreate(context, container)
    this.context = context

    val episodes = store.episodes(podcastID)
    layout = DetailsLayout(
        context.activity,
        podcast.value!!,
        episodes.value,
        taskRunner,
        object : DetailsLayout.Callbacks {
          override fun onEpisodePlay(podcast: Podcast, episode: Episode) {
            MediaServiceClient.i.play(podcast, episode)
          }

          override fun onEpisodeDetails(podcast: Podcast, episode: Episode) {
            context.pushScreen<EpisodeDetailsScreen>(EpisodeDetailsScreen.Data(podcast, episode))
          }
        })

    podcast.observe(this, Observer {
      p -> layout?.refresh(p, episodes.value)
    })

    episodes.observe(this, Observer {
      e -> layout?.refresh(podcast.value!!, episodes.value)
    })
  }

  override fun onShow(): View? {
    super.onShow()
    return layout
  }
}
