package au.com.codeka.podcreep.app.podcasts.subscriptions

import android.content.Context
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
import au.com.codeka.podcreep.databinding.SubEpisodesDateRowBinding
import au.com.codeka.podcreep.databinding.SubEpisodesRowBinding
import au.com.codeka.podcreep.model.cache.EpisodeMediaCache
import au.com.codeka.podcreep.model.store.Episode
import au.com.codeka.podcreep.model.store.Podcast
import au.com.codeka.podcreep.model.store.Subscription
import java.lang.RuntimeException
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.HashMap

open class BaseEpisodeListTabLayout(
    context: Context,
    lifecyleOwner: LifecycleOwner,
    subscriptions: LiveData<List<Subscription>>,
    episodes: LiveData<List<Episode>>,
    callbacks: SubscriptionsLayout.Callbacks)
  : FrameLayout(context) {

  companion object {
    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM, yyyy")
  }

  val model: Model
  val binding: SubEpisodesBinding
  val adapter: Adapter

  init {
    val inflater = LayoutInflater.from(context)
    binding = SubEpisodesBinding.inflate(inflater, this, true)

    adapter = Adapter(callbacks, App.i.mediaCache)
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

  class Adapter(private val callbacks: SubscriptionsLayout.Callbacks,
                private val mediaCache: EpisodeMediaCache)
    : RecyclerView.Adapter<ViewHolder>() {

    private val rows: ArrayList<Row> = ArrayList()

    fun refresh(podcasts: HashMap<Long, LiveData<Podcast>>, episodes: List<Episode>) {
      var lastDate: ZonedDateTime? = null
      for (ep in episodes) {
        val epDt = ep.pubDate.toInstant().atZone(ZoneOffset.systemDefault())
        if (lastDate == null || epDt.year != lastDate.year || epDt.dayOfYear != lastDate.dayOfYear) {
          rows.add(Row(epDt))
          lastDate = epDt
        }

        val podcast = podcasts[ep.podcastID]
        if (podcast == null || podcast.value == null) {
          continue
        }
        rows.add(Row(podcast.value!!, ep))
      }
      notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
      val inflater = LayoutInflater.from(parent.context)
      return when (viewType) {
        0 -> {
          val binding = SubEpisodesDateRowBinding.inflate(inflater, parent, false)
          ViewHolder(binding, callbacks, mediaCache)
        }
        1 -> {
          val binding = SubEpisodesRowBinding.inflate(inflater, parent, false)
          ViewHolder(binding, callbacks, mediaCache)
        }
        else -> {
          throw RuntimeException("Unexpected viewType: $viewType")
        }
      }
    }

    override fun getItemCount(): Int {
      return rows.size
    }

    override fun getItemViewType(position: Int): Int {
      return rows[position].viewType
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
      holder.bind(rows[position])
    }
  }

  class ViewHolder : RecyclerView.ViewHolder {
    private val mediaCache: EpisodeMediaCache
    private val callbacks: SubscriptionsLayout.Callbacks
    private val epBinding: SubEpisodesRowBinding?
    private val dtBinding: SubEpisodesDateRowBinding?

    constructor(binding: SubEpisodesRowBinding, callbacks: SubscriptionsLayout.Callbacks,
                mediaCache: EpisodeMediaCache)
        : super(binding.root) {
      this.callbacks = callbacks
      this.mediaCache = mediaCache
      this.epBinding = binding
      dtBinding = null
    }

    constructor(binding: SubEpisodesDateRowBinding, callbacks: SubscriptionsLayout.Callbacks,
                mediaCache: EpisodeMediaCache)
        : super(binding.root) {
      this.callbacks = callbacks
      this.mediaCache = mediaCache
      this.dtBinding = binding
      epBinding = null
    }

    fun bind(row: Row) {
      if (epBinding != null) {
        epBinding.callbacks = callbacks
        epBinding.iconCache = App.i.iconCache // TODO: pass this in
        epBinding.vm = EpisodeRowViewModel(row.podcast!!, row.episode!!, mediaCache)
        epBinding.executePendingBindings()
      } else if (dtBinding != null) {
        dtBinding.dt = row.date
        dtBinding.fmt = dateFormatter
        dtBinding.executePendingBindings()
      }
    }
  }

  class Row {
    val viewType: Int
    val date: ZonedDateTime?
    val podcast: Podcast?
    val episode: Episode?

    constructor(dt: ZonedDateTime) {
      viewType = 0
      date = dt
      podcast = null
      episode = null
    }

    constructor(p: Podcast, ep: Episode) {
      viewType = 1
      date = null
      podcast = p
      episode = ep
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
