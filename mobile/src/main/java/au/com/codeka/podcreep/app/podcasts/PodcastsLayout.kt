package au.com.codeka.podcreep.app.podcasts

import android.content.Context
import android.view.View
import android.widget.RelativeLayout
import au.com.codeka.podcreep.R
import kotlinx.android.synthetic.main.podcasts.view.*

class PodcastsLayout(context: Context, var callbacks: Callbacks) : RelativeLayout(context) {
  interface Callbacks {
    fun onFoo()
  }

  init {
    View.inflate(context, R.layout.podcasts, this)
    viewpager.adapter = TabPagerAdapter(context)
    tab_layout.setupWithViewPager(viewpager)
  }
}
