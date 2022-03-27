package com.podcreep.app.podcasts.discover

import android.content.Context
import android.view.View
import android.widget.RelativeLayout
import androidx.lifecycle.LiveData
import com.podcreep.R
import com.podcreep.concurrency.TaskRunner
import com.podcreep.model.store.Podcast
import kotlinx.android.synthetic.main.discover.view.*

class DiscoverLayout(
    context: Context,
    taskRunner: TaskRunner,
    callbacks: Callbacks)
  : RelativeLayout(context) {

  interface Callbacks {
    fun onViewPodcastClick(podcast: LiveData<Podcast>)
  }

  init {
    View.inflate(context, R.layout.discover, this)
    viewpager.adapter = TabPagerAdapter(context, taskRunner, callbacks)
    tab_layout.setupWithViewPager(viewpager)
  }
}
