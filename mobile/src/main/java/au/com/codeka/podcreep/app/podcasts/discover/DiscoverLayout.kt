package au.com.codeka.podcreep.app.podcasts.discover

import android.content.Context
import android.view.View
import android.widget.RelativeLayout
import androidx.lifecycle.LiveData
import au.com.codeka.podcreep.R
import au.com.codeka.podcreep.concurrency.TaskRunner
import au.com.codeka.podcreep.model.store.Podcast
import au.com.codeka.podcreep.model.sync.PodcastInfo
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
