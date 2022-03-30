package com.podcreep.app.podcasts.episode

import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import com.podcreep.R
import com.podcreep.concurrency.TaskRunner
import com.podcreep.model.cache.EpisodeMediaCache
import com.podcreep.model.store.Episode
import com.podcreep.model.store.Podcast
import com.podcreep.model.store.Store
import com.podcreep.ui.Screen
import com.podcreep.ui.ScreenContext
import com.podcreep.ui.ScreenOptions

class EpisodeDetailsScreen(
    private val taskRunner: TaskRunner,
    private val store: Store,
    private val mediaCache: EpisodeMediaCache,
    private var podcast: Podcast,
    private var episode: Episode)
  : Screen() {
  private var layout: EpisodeDetailsLayout? = null

  override val options: ScreenOptions
    get() = ScreenOptions(actionBarMenu = R.menu.episode_details_menu)

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

  override fun onMenuItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      R.id.mark_done -> {
        // TODO: mark this episode done
        return true
      }
      R.id.download -> {
        mediaCache.download(podcast, episode)
        return true
      }
      else -> return false
    }
  }

  class Data (val podcast: LiveData<Podcast>, val episode: Episode)
}