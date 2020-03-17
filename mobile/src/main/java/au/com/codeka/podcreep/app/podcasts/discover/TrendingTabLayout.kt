package au.com.codeka.podcreep.app.podcasts.discover

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import au.com.codeka.podcreep.concurrency.TaskRunner
import au.com.codeka.podcreep.concurrency.Threads
import au.com.codeka.podcreep.databinding.DiscoverTrendingRowBinding
import au.com.codeka.podcreep.model.store.Podcast
import au.com.codeka.podcreep.model.sync.PodcastInfo
import au.com.codeka.podcreep.model.sync.PodcastListOld
import au.com.codeka.podcreep.net.HttpRequest
import au.com.codeka.podcreep.net.Server

class TrendingTabLayout(
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
      var resp = request.execute<PodcastListOld>()
      taskRunner.runTask({
        adapter = Adapter(resp.podcasts, callbacks)
      }, Threads.UI)
    }, Threads.BACKGROUND)
  }

  class Adapter(
      private val dataset: List<PodcastInfo>,
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

    fun bind(podcast: PodcastInfo) {
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
