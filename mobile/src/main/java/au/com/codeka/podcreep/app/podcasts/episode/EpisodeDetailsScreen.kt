package au.com.codeka.podcreep.app.podcasts.episode

import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import au.com.codeka.podcreep.concurrency.TaskRunner
import au.com.codeka.podcreep.model.store.Episode
import au.com.codeka.podcreep.model.store.Podcast
import au.com.codeka.podcreep.model.store.Store
import au.com.codeka.podcreep.ui.Screen
import au.com.codeka.podcreep.ui.ScreenContext

class EpisodeDetailsScreen(
    private val taskRunner: TaskRunner,
    private val store: Store,
    private var podcast: LiveData<Podcast>,
    private var episode: LiveData<Episode>)
  : Screen() {
  private var layout: EpisodeDetailsLayout? = null

  override fun onCreate(context: ScreenContext, container: ViewGroup) {
    super.onCreate(context, container)

    layout = EpisodeDetailsLayout(
        context.activity,
        podcast,
        episode,
        taskRunner,
        object : EpisodeDetailsLayout.Callbacks {
          override fun onMarkDone() {
            // TODO: stuff
          }
        })
  }

  override fun onShow(): View? {
    super.onShow()
    return layout
  }
}