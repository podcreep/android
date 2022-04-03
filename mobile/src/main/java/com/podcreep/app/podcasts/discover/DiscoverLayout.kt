package com.podcreep.app.podcasts.discover

import android.content.Context
import android.view.LayoutInflater
import android.widget.RelativeLayout
import androidx.lifecycle.LiveData
import com.podcreep.concurrency.TaskRunner
import com.podcreep.databinding.DiscoverBinding
import com.podcreep.model.store.Podcast

class DiscoverLayout(
    context: Context,
    taskRunner: TaskRunner,
    callbacks: Callbacks)
  : RelativeLayout(context) {

  interface Callbacks {
    fun onViewPodcastClick(podcast: LiveData<Podcast>)
  }

  init {
    val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    val binding = DiscoverBinding.inflate(layoutInflater, this)
    binding.viewpager.adapter = TabPagerAdapter(context, taskRunner, callbacks)
    binding.tabLayout.setupWithViewPager(binding.viewpager)
  }
}
