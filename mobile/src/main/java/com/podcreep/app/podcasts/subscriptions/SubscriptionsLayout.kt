package com.podcreep.app.podcasts.subscriptions

import android.content.Context
import android.view.LayoutInflater
import android.widget.RelativeLayout
import androidx.annotation.Keep
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import com.podcreep.app.podcasts.episode.BaseEpisodeListLayout
import com.podcreep.concurrency.TaskRunner
import com.podcreep.databinding.SubscriptionsBinding
import com.podcreep.model.store.Podcast
import com.podcreep.model.store.Store

class SubscriptionsLayout @Keep constructor(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    store: Store,
    taskRunner: TaskRunner,
    callbacks: Callbacks)
  : RelativeLayout(context) {

  interface Callbacks : BaseEpisodeListLayout.Callbacks {
    fun onViewPodcastClick(podcast: LiveData<Podcast>)
  }

  init {
    val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    val binding = SubscriptionsBinding.inflate(layoutInflater, this)

    binding.viewpager.adapter = TabPagerAdapter(context, lifecycleOwner, store, taskRunner, callbacks)
    binding.tabLayout.setupWithViewPager(binding.viewpager)
  }
}
