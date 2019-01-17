package au.com.codeka.podcreep.app.podcasts.discover

import android.content.Context
import android.view.View
import android.widget.RelativeLayout
import au.com.codeka.podcreep.R
import au.com.codeka.podcreep.concurrency.TaskRunner
import kotlinx.android.synthetic.main.discover.view.*

class DiscoverLayout(
    context: Context,
    taskRunner: TaskRunner,
    var callbacks: Callbacks)
  : RelativeLayout(context) {

  interface Callbacks {
    fun onFoo()
  }

  init {
    View.inflate(context, R.layout.discover, this)
    viewpager.adapter = TabPagerAdapter(context, taskRunner)
    tab_layout.setupWithViewPager(viewpager)
  }
}
