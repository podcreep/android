package au.com.codeka.podcreep.app.podcasts.subscriptions

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import au.com.codeka.podcreep.App
import au.com.codeka.podcreep.R
import au.com.codeka.podcreep.databinding.SubEpisodesBinding
import au.com.codeka.podcreep.databinding.SubEpisodesRowBinding
import au.com.codeka.podcreep.model.store.Episode
import au.com.codeka.podcreep.model.store.Podcast
import au.com.codeka.podcreep.model.store.Subscription

open class BaseEpisodeListTabLayout(
    context: Context,
    private val lifecyleOwner: LifecycleOwner,
    private val subscriptions: LiveData<List<Subscription>>,
    private val episodes: LiveData<List<Episode>>,
    private val callbacks: SubscriptionsLayout.Callbacks)
  : FrameLayout(context) {

  val model: Model
  val binding: SubEpisodesBinding
  val adapter: Adapter

  // We'll keep track of all the episodes we're displaying. If we get an update and nothing's
  // changed then we can skip refreshing it.
  var visibleEpisodes: List<Episode>? = null

  init {
    val inflater = LayoutInflater.from(context)
    binding = SubEpisodesBinding.inflate(inflater, this, true)

    adapter = Adapter(callbacks)
    val episodesList = findViewById<RecyclerView>(R.id.episodes)
    episodesList.layoutManager = LinearLayoutManager(context)
    episodesList.adapter = adapter

    model = Model(subscriptions, episodes)
    model.observe(lifecyleOwner, Observer {
      m -> refresh(m.first, m.second)
    })
  }

  fun refresh(subscriptions: List<Subscription>?, episodes: List<Episode>?) {
    if (subscriptions == null || episodes == null) {
      // This'll happen before we've finished loading both.
      return
    }

    binding.iconCache = App.i.iconCache
    binding.executePendingBindings()

    val podcasts = HashMap<Long, LiveData<Podcast>>()
    for (sub in subscriptions) {
      podcasts[sub.podcastID] = sub.podcast
    }

    // Maybe not all the subscriptions have loaded yet?
    for (ep in episodes) {
      if (!podcasts.containsKey(ep.podcastID)) {
        return
      }
    }

    val episodesList = findViewById<RecyclerView>(R.id.episodes)
    adapter.refresh(podcasts, episodes)
  }

  class Adapter(private val callbacks: SubscriptionsLayout.Callbacks)
    : RecyclerView.Adapter<ViewHolder>() {

    private var podcasts: HashMap<Long, LiveData<Podcast>>? = null
    private var episodes: List<Episode>? = null

    fun refresh(podcasts: HashMap<Long, LiveData<Podcast>>, episodes: List<Episode>) {
      this.podcasts = podcasts
      this.episodes = episodes
      notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
      val inflater = LayoutInflater.from(parent.context)
      val binding = SubEpisodesRowBinding.inflate(inflater, parent, false)

      return ViewHolder(binding, podcasts!!, callbacks)
    }

    override fun getItemCount(): Int {
      if (podcasts == null) {
        return 0
      }
      return episodes?.size ?: 0
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
      holder.bind(episodes!![position])
    }
  }

  class ViewHolder(
      val binding: SubEpisodesRowBinding, val podcasts: HashMap<Long, LiveData<Podcast>>,
      val callbacks: SubscriptionsLayout.Callbacks)
    : RecyclerView.ViewHolder(binding.root) {

    fun bind(episode: Episode) {
      binding.callbacks = callbacks
      Log.i("DEANH", "Doing episode '${episode.title}' from ${episode.podcastID}")
      binding.vm = EpisodeRowViewModel(podcasts[episode.podcastID]!!, episode)
      binding.executePendingBindings()
    }
  }

  class Model(
      subscriptionsLiveData: LiveData<List<Subscription>>,
      episodesLiveData: LiveData<List<Episode>>)
    : MediatorLiveData<Pair<List<Subscription>?, List<Episode>?>>() {
    var subscriptions: List<Subscription>? = null
    var episodes: List<Episode>? = null

    init {
      super.addSource(subscriptionsLiveData) {
        subscriptions = it
        value = Pair(subscriptions, episodes)
      }
      super.addSource(episodesLiveData) {
        episodes = it
        value = Pair(subscriptions, episodes)
      }
    }
  }
}
