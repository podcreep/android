package au.com.codeka.podcreep.app.podcasts.subscriptions

import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import au.com.codeka.podcreep.App
import au.com.codeka.podcreep.app.podcasts.podcast.PodcastDetailsScreen
import au.com.codeka.podcreep.app.podcasts.episode.EpisodeDetailsScreen
import au.com.codeka.podcreep.app.service.MediaServiceClient
import au.com.codeka.podcreep.model.store.Episode
import au.com.codeka.podcreep.model.store.Podcast
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

