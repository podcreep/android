package com.podcreep.app.podcasts.podcast

import android.content.Context
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import com.podcreep.App
import com.podcreep.R
import com.podcreep.app.podcasts.episode.BaseEpisodeListLayout
import com.podcreep.concurrency.TaskRunner
import com.podcreep.databinding.PodcastDetailsBinding
import com.podcreep.model.store.Episode
import com.podcreep.model.store.Podcast
import com.podcreep.model.store.Subscription

class PodcastDetailsLayout(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    podcast: LiveData<Podcast>,
    episodes: LiveData<List<Episode>>,
    taskRunner: TaskRunner,
    private val callbacks: Callbacks)
  : FrameLayout(context) {

  val binding: PodcastDetailsBinding
  private val episodeListLayout: EpisodeListLayout

  interface Callbacks: BaseEpisodeListLayout.Callbacks {
  }

  init {
    val inflater = LayoutInflater.from(context)
    binding = PodcastDetailsBinding.inflate(inflater, this, true)

    val subscriptionsLiveData = MediatorLiveData<List<Subscription>>()
    subscriptionsLiveData.addSource(podcast) {
        p -> run {
        val sub = Subscription(p.id)
        sub.podcast.value = p
        val subscriptions = ArrayList<Subscription>()
        subscriptions.add(sub)
        subscriptionsLiveData.value = subscriptions
      }
    }

    episodeListLayout = EpisodeListLayout(
        context, lifecycleOwner, subscriptionsLiveData, episodes, callbacks)
    findViewById<FrameLayout>(R.id.episode_list).addView(episodeListLayout)

    podcast.observe(lifecycleOwner, Observer {
      p -> run {
        refresh(p)
      }
    })
  }

  fun refresh(podcast: Podcast) {
    binding.podcast = podcast
    binding.iconCache = App.i.iconCache
    binding.executePendingBindings()
  }

  class EpisodeListLayout(context: Context, lifecycleOwner: LifecycleOwner,
                          subscriptions: LiveData<List<Subscription>>,
                          episodes: LiveData<List<Episode>>,
                          callbacks: Callbacks)
    : BaseEpisodeListLayout(context, lifecycleOwner, subscriptions, episodes, callbacks) {
  }
}
