package com.podcreep.app.podcasts.subscriptions

import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.viewpager.widget.PagerAdapter
import com.podcreep.concurrency.TaskRunner
import com.podcreep.model.store.Store

class TabPagerAdapter(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val store: Store,
    private val taskRunner: TaskRunner,
    private val callbacks: SubscriptionsLayout.Callbacks)
  : PagerAdapter() {

  override fun instantiateItem(container: ViewGroup, position: Int): Any {
    val tab = Tabs.values()[position]
    Log.i("DEANH", String.format("tab=%s class=%s, constructors=%s", tab, tab.layoutClass, tab.layoutClass.constructors))
    val view = tab.layoutClass.constructors.first().call(
        context, lifecycleOwner, store, taskRunner, callbacks)
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