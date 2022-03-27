package com.podcreep.app.podcasts.subscriptions

import android.view.View
import com.podcreep.R
import kotlin.reflect.KClass

enum class Tabs(
    private val _titleResId: Int,
    private val _layoutClass: KClass<out View>) {
  NEW_RELEASES(R.string.new_releases, NewReleasesTabLayout::class),
  IN_PROGRESS(R.string.in_progress, InProgressTabLayout::class),
  PODCASTS(R.string.podcasts, PodcastsTabLayout::class);

  val titleResId: Int
    get() = _titleResId

  val layoutClass: KClass<out View>
    get() = _layoutClass
}
