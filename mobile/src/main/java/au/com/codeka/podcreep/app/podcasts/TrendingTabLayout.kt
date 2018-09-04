package au.com.codeka.podcreep.app.podcasts

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import au.com.codeka.podcreep.databinding.PodcastsTrendingRowBinding
import au.com.codeka.podcreep.model.Podcast

class TrendingTabLayout(context: Context): RecyclerView(context) {
  private val _layoutManager = LinearLayoutManager(context)

  init {
    setHasFixedSize(true)
    layoutManager = _layoutManager
    adapter = Adapter(arrayOf(
        Podcast("The Daily Zeitgeist", "Some description", "http://static.megaphone.fm/podcasts/052418f4-2d44-11e8-805b-9780d43c8144/image/uploads_2F1521663074285-ayw5ru3yj4p-ce4a614fc6624e78ce25c33eb8273e88_2Fdaily-zeitgeist-hero.png"),
        Podcast("The Daily", "Another description", "https://dfkfj8j276wwv.cloudfront.net/images/01/1b/f3/d6/011bf3d6-a448-4533-967b-e2f19e376480/7fdd4469c1b5cb3b66aa7dcc9fa21f138efe9a0310a8a269f3dcd07c83a552844fcc445ea2d53db1e55d6fb077aeaa8a1566851f8f2d8ac4349d9d23a87a69f5.jpeg"),
        Podcast("Hack", "Blah", "http://www.abc.net.au/cm/rimage/9080954-1x1-thumbnail.jpg?v=2")
    ))
  }

  class Adapter(private val dataset: Array<Podcast>): RecyclerView.Adapter<TrendingTabLayout.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
      val inflater = LayoutInflater.from(parent.context)
      val binding = PodcastsTrendingRowBinding.inflate(inflater, parent, false)
      return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
      Log.i("DEANH", "Got ${dataset.size} items")
      return dataset.size
    }

    override fun onBindViewHolder(holder: TrendingTabLayout.ViewHolder, position: Int) {
      holder.bind(dataset[position])
    }
  }

  class ViewHolder(val binding: PodcastsTrendingRowBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(podcast: Podcast) {
      binding.podcast = podcast
      binding.executePendingBindings()
    }
  }
}
