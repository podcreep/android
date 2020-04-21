package au.com.codeka.podcreep.app.podcasts.subscriptions

import android.content.Context
import android.view.View
import android.widget.RelativeLayout
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import au.com.codeka.podcreep.R
import au.com.codeka.podcreep.app.podcasts.episode.BaseEpisodeListLayout
import au.com.codeka.podcreep.concurrency.TaskRunner
import au.com.codeka.podcreep.model.store.Episode
import au.com.codeka.podcreep.model.store.Podcast
import au.com.codeka.podcreep.model.store.Store
import kotlinx.android.synthetic.main.discover.view.*

class SubscriptionsLayout(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    store: Store,
    taskRunner: TaskRunner,
    callbacks: Callbacks)
  : RelativeLayout(context) {

  interface Callbacks : BaseEpisodeListLayout.Callbacks {
    fun onViewPodcastClick(podcast: LiveData<Podcast>)
  }

  init {
    View.inflate(context, R.layout.subscriptions, this)
    viewpager.adapter = TabPagerAdapter(context, lifecycleOwner, store, taskRunner, callbacks)
    tab_layout.setupWithViewPager(viewpager)
  }
}
