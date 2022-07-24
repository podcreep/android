package com.podcreep.app.podcasts.discover

import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import com.podcreep.app.podcasts.podcast.PodcastDetailsScreen
import com.podcreep.concurrency.TaskRunner
import com.podcreep.model.store.Podcast
import com.podcreep.model.store.Store
import com.podcreep.ui.Screen
import com.podcreep.ui.ScreenContext
import com.podcreep.ui.ScreenOptions

class DiscoverScreen(private val taskRunner: TaskRunner, private val store: Store): Screen() {
  private var layout: DiscoverLayout? = null

  override val options: ScreenOptions
    get() = ScreenOptions(isRootScreen = true)

  override fun onCreate(context: ScreenContext, container: ViewGroup) {
    super.onCreate(context, container)

    layout = DiscoverLayout(context.activity, taskRunner, object : DiscoverLayout.Callbacks {
      override fun onViewPodcastClick(podcast: LiveData<Podcast>) {
        context.pushScreen<PodcastDetailsScreen>(/*sharedViews=*/null, podcast)
      }
    })
  }

  override fun onShow(): View? {
    super.onShow()
    return layout
  }
}
