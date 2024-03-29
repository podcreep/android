package com.podcreep.app.podcasts.discover

import android.content.Context
import androidx.viewpager.widget.PagerAdapter
import android.view.View
import android.view.ViewGroup
import com.podcreep.concurrency.TaskRunner

class TabPagerAdapter(
    private val context: Context,
    private val taskRunner: TaskRunner,
    private val callback: DiscoverLayout.Callbacks)
  : PagerAdapter() {

  override fun instantiateItem(container: ViewGroup, position: Int): Any {
    val tab = Tabs.values()[position]
    val view = tab.layoutClass.constructors.first().call(context, taskRunner, callback)
    container.addView(view)
    return view
  }

  override fun destroyItem(collection: ViewGroup, position: Int, view: Any) {
    collection.removeView(view as View)
  }

  override fun isViewFromObject(view: View, obj: Any): Boolean {
    return view == obj
  }

  override fun getCount(): Int {
    return Tabs.values().size
  }

  override fun getPageTitle(position: Int): CharSequence? {
    val tab = Tabs.values()[position]
    return context.getString(tab.titleResId)
  }
}