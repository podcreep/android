package au.com.codeka.podcreep.app.podcasts.discover

import android.view.View
import au.com.codeka.podcreep.R
import kotlin.reflect.KClass

enum class Tabs(
    private val _titleResId: Int,
    private val _layoutClass: KClass<out View>) {
  TRENDING(R.string.trending, TrendingTabLayout::class),
  SEARCH(R.string.search, SearchTabLayout::class);

  val titleResId: Int
    get() = _titleResId

  val layoutClass: KClass<out View>
    get() = _layoutClass
}
