package au.com.codeka.podcreep.app.podcasts.discover

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import au.com.codeka.podcreep.concurrency.TaskRunner
import au.com.codeka.podcreep.concurrency.Threads
import au.com.codeka.podcreep.databinding.DiscoverTrendingRowBinding
import au.com.codeka.podcreep.model.sync.PodcastOld
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
      private val dataset: List<PodcastOld>,
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

    fun bind(podcast: PodcastOld) {
      binding.podcast = podcast
      binding.executePendingBindings()
      binding.root.setOnClickListener {
        run {
          callbacks.onViewPodcastClick(podcast)
        }
      }
    }
  }
}
