package au.com.codeka.podcreep.app.podcasts.subscriptions

import android.content.Context
import android.view.View
import android.widget.RelativeLayout
import au.com.codeka.podcreep.R
import au.com.codeka.podcreep.concurrency.TaskRunner

class SubscriptionsLayout(
    context: Context,
    taskRunner: TaskRunner,
    var callbacks: Callbacks)
  : RelativeLayout(context) {

  interface Callbacks {
    fun onFoo()
  }

  init {
    View.inflate(context, R.layout.subscriptions, this)
    //viewpager.adapter = TabPagerAdapter(context, taskRunner, callbacks)
   //tab_layout.setupWithViewPager(viewpager)
  }
}
