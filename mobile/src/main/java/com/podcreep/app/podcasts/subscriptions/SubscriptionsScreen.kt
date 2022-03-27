package com.podcreep.app.podcasts.subscriptions

import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import com.podcreep.App
import com.podcreep.app.podcasts.podcast.PodcastDetailsScreen
import com.podcreep.app.podcasts.episode.EpisodeDetailsScreen
import com.podcreep.app.service.MediaServiceClient
import com.podcreep.model.store.Episode
import com.podcreep.model.store.Podcast
import com.podcreep.model.store.Store
import com.podcreep.ui.Screen
import com.podcreep.ui.ScreenContext
import com.podcreep.ui.ScreenOptions

class SubscriptionsScreen(private val store: Store): Screen() {
  private var layout: SubscriptionsLayout? = null

  override val options: ScreenOptions
    get() = ScreenOptions(isRootScreen = true)

  override fun onCreate(context: ScreenContext, container: ViewGroup) {
    super.onCreate(context, container)

    layout = SubscriptionsLayout(
        context.activity,
        this,
        store,
        App.i.taskRunner,
        callbacks = object : SubscriptionsLayout.Callbacks {
          override fun onEpisodeDetails(podcast: Podcast, episode: Episode) {
            context.pushScreen<EpisodeDetailsScreen>(EpisodeDetailsScreen.Data(podcast, episode))
          }

          override fun onEpisodePlay(podcast: Podcast, episode: Episode) {
            MediaServiceClient.i.play(podcast, episode)
          }

          override fun onViewPodcastClick(podcast: LiveData<Podcast>) {
            context.pushScreen<PodcastDetailsScreen>(podcast)
          }
        })
  }

  override fun onShow(): View? {
    super.onShow()
    return layout
  }
}

