package com.podcreep.app.podcasts.episode

import android.content.Context
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.podcreep.App
import com.podcreep.concurrency.TaskRunner
import com.podcreep.databinding.EpisodeDetailsBinding
import com.podcreep.model.store.Episode
import com.podcreep.model.store.Podcast

class EpisodeDetailsLayout (
    context: Context,
    podcast: Podcast,
    episode: Episode,
    taskRunner: TaskRunner,
    private val callbacks: Callbacks)
  : FrameLayout(context) {

  val binding: EpisodeDetailsBinding

  // TODO
  interface Callbacks {
    fun onMarkDone()
  }

  init {
    val inflater = LayoutInflater.from(context)
    binding = EpisodeDetailsBinding.inflate(inflater, this, true)
    binding.podcast = podcast
    binding.episode = episode
    binding.iconCache = App.i.iconCache
  }
}