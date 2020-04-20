package au.com.codeka.podcreep.app.podcasts.episode

import android.content.Context
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.lifecycle.LiveData
import au.com.codeka.podcreep.App
import au.com.codeka.podcreep.concurrency.TaskRunner
import au.com.codeka.podcreep.databinding.EpisodeDetailsBinding
import au.com.codeka.podcreep.model.store.Episode
import au.com.codeka.podcreep.model.store.Podcast

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