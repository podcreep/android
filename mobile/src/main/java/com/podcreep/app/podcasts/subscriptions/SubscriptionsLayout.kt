package com.podcreep.app.podcasts.subscriptions

import android.content.Context
import android.view.View
import android.widget.RelativeLayout
import androidx.annotation.Keep
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import com.podcreep.R
import com.podcreep.app.podcasts.episode.BaseEpisodeListLayout
import com.podcreep.concurrency.TaskRunner
import com.podcreep.model.store.Episode
import com.podcreep.model.store.Podcast
import com.podcreep.model.store.Store
import kotlinx.android.synthetic.main.discover.view.*

class SubscriptionsLayout @Keep constructor(
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
