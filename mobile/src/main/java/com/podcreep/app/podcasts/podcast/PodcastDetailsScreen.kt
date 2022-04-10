package com.podcreep.app.podcasts.podcast

import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import com.podcreep.App
import com.podcreep.app.podcasts.episode.EpisodeDetailsScreen
import com.podcreep.concurrency.TaskRunner
import com.podcreep.model.store.Episode
import com.podcreep.model.store.Podcast
import com.podcreep.model.store.Store
import com.podcreep.ui.Screen
import com.podcreep.ui.ScreenContext

/**
 * PodcastScreen is shown when you click a podcast in the "Podcasts" list. We'll show the episode
 * list for that podcast.
 */
class PodcastDetailsScreen(
      private val taskRunner: TaskRunner,
      private val store: Store,
      private val podcastID: Long,
      private var podcast: LiveData<Podcast>)
    : Screen() {

  private var layout: PodcastDetailsLayout? = null
  private lateinit var context: ScreenContext

  override fun onCreate(context: ScreenContext, container: ViewGroup) {
    super.onCreate(context, container)
    this.context = context

    val episodes = store.episodes(podcastID)
    layout = PodcastDetailsLayout(
        context.activity,
        context.activity,
        podcast,
        episodes,
        taskRunner,
        object : PodcastDetailsLayout.Callbacks {
          override fun onEpisodePlay(podcast: Podcast, episode: Episode) {
            App.i.mediaServiceClient.play(podcast, episode)
          }

          override fun onEpisodeDetails(podcast: LiveData<Podcast>, episode: Episode) {
            context.pushScreen<EpisodeDetailsScreen>(EpisodeDetailsScreen.Data(podcast, episode))
          }
        })
  }

  override fun onShow(): View? {
    super.onShow()
    return layout
  }
}
