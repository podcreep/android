package au.com.codeka.podcreep.app.podcasts

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.util.Log
import android.view.View
import android.view.ViewGroup

class TabPagerAdapter(private val context: Context): PagerAdapter() {

  override fun instantiateItem(container: ViewGroup, position: Int): Any {
    Log.i("DEANH", "instantiating item for position $position")
    val tab = Tabs.values()[position]
    val view = tab.layoutClass.constructors.first().call(context)
    container.addView(view)
    return view
  }

  override fun destroyItem(collection: ViewGroup, position: Int, view: Any) {
    Log.i("DEANH", "destroying item for position $position")
    collection.removeView(view as View)
  }

  override fun isViewFromObject(view: View, obj: Any): Boolean {
    return view == obj
  }

  override fun getCount(): Int {
    return Tabs.values().size
  }

  override fun getPageTitle(position: Int): CharSequence? {
    Log.i("DEANH", "getting title for position $position")
    val tab = Tabs.values()[position]
    return context.getString(tab.titleResId)
  }
}