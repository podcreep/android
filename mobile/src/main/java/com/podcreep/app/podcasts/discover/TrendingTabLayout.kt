package com.podcreep.app.podcasts.discover

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.lifecycle.MutableLiveData
import com.podcreep.concurrency.TaskRunner
import com.podcreep.concurrency.Threads
import com.podcreep.databinding.DiscoverTrendingRowBinding
import com.podcreep.model.store.Podcast
import com.podcreep.model.sync.data.PodcastJson
import com.podcreep.model.sync.data.PodcastListJson
import com.podcreep.net.HttpRequest
import com.podcreep.net.Server

class TrendingTabLayout @Keep constructor(
    context: Context,
    private val taskRunner: TaskRunner,
    private val callbacks: DiscoverLayout.Callbacks)
  : RecyclerView(context) {

  private val _layoutManager = LinearLayoutManager(context)

  init {
    setHasFixedSize(true)
    layoutManager = _layoutManager


    taskRunner.runTask({
      val request = Server.request("/api/podcasts")
          .method(HttpRequest.Method.GET)
          .build()
      var resp = request.execute<PodcastListJson>()
      taskRunner.runTask({
        adapter = Adapter(resp.podcasts, callbacks)
      }, Threads.UI)
    }, Threads.BACKGROUND)
  }

  class Adapter(
    private val dataset: List<PodcastJson>,
    private val callbacks: DiscoverLayout.Callbacks)
    : RecyclerView.Adapter<ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
      val inflater = LayoutInflater.from(parent.context)
      val binding = DiscoverTrendingRowBinding.inflate(inflater, parent, false)
      return ViewHolder(binding, callbacks)
    }

    override fun getItemCount(): Int {
      return dataset.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
      holder.bind(dataset[position])
    }
  }

  class ViewHolder(val binding: DiscoverTrendingRowBinding, val callbacks: DiscoverLayout.Callbacks)
    : RecyclerView.ViewHolder(binding.root) {

    fun bind(podcast: PodcastJson) {
      binding.podcast = podcast
      binding.executePendingBindings()
      binding.root.setOnClickListener {
        run {
          val ld = MutableLiveData<Podcast>()
          ld.value = Podcast(id = podcast.id, title = podcast.title, description = podcast.description, imageUrl = podcast.imageUrl)
          callbacks.onViewPodcastClick(ld)
        }
      }
    }
  }
}
