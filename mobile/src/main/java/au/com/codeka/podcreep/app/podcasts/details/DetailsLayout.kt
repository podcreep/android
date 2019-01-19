package au.com.codeka.podcreep.app.podcasts.details

import android.content.Context
import android.view.LayoutInflater
import android.widget.FrameLayout
import au.com.codeka.podcreep.concurrency.TaskRunner
import au.com.codeka.podcreep.databinding.DetailsBinding
import au.com.codeka.podcreep.model.Podcast

class DetailsLayout(
    context: Context,
    podcast: Podcast,
    taskRunner: TaskRunner,
    callbacks: Callbacks)
  : FrameLayout(context) {

  interface Callbacks {
    fun onFoo()
  }

  init {
    val inflater = LayoutInflater.from(context)
    val binding = DetailsBinding.inflate(inflater, this, true)
    binding.podcast = podcast
    binding.executePendingBindings()
  }
}
