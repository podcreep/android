package au.com.codeka.podcreep.app.podcasts.details

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import au.com.codeka.podcreep.R
import au.com.codeka.podcreep.concurrency.TaskRunner
import au.com.codeka.podcreep.databinding.DetailsBinding
import au.com.codeka.podcreep.databinding.DetailsEpisodeRowBinding
import au.com.codeka.podcreep.model.Episode
import au.com.codeka.podcreep.model.Podcast


class DetailsLayout(
    context: Context,
    podcast: Podcast,
    taskRunner: TaskRunner,
    private val callbacks: Callbacks)
  : FrameLayout(context) {

  val binding: DetailsBinding

  interface Callbacks {
    fun onEpisodePlay(podcast: Podcast, episode: Episode)
  }

  init {
    val inflater = LayoutInflater.from(context)
    binding = DetailsBinding.inflate(inflater, this, true)

    val episodesList = findViewById<RecyclerView>(R.id.episodes)
    episodesList.layoutManager = LinearLayoutManager(context)

    refresh(podcast)
  }

  fun refresh(podcast: Podcast) {
    binding.podcast = podcast
    binding.executePendingBindings()

    val episodesList = findViewById<RecyclerView>(R.id.episodes)
    episodesList.adapter = Adapter(podcast, callbacks)
  }

  class Adapter(
      private val podcast: Podcast,
      private val callbacks: Callbacks)
    : RecyclerView.Adapter<ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
      val inflater = LayoutInflater.from(parent.context)
      val binding = DetailsEpisodeRowBinding.inflate(inflater, parent, false)
      return ViewHolder(binding, callbacks)
    }

    override fun getItemCount(): Int {
      return if (podcast.episodes == null) 0 else return podcast.episodes.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
      holder.bind(podcast, podcast.episodes!![position])
    }
  }

  class ViewHolder(val binding: DetailsEpisodeRowBinding, val callbacks: Callbacks)
    : RecyclerView.ViewHolder(binding.root) {

    fun bind(podcast: Podcast, episode: Episode) {
      binding.callbacks = callbacks
      binding.podcast = podcast
      binding.episode = episode
      binding.executePendingBindings()
    }
  }

}
