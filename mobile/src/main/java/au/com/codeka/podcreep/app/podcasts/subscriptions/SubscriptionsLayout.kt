package au.com.codeka.podcreep.app.podcasts.subscriptions

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import au.com.codeka.podcreep.R
import au.com.codeka.podcreep.databinding.SubscriptionsRowBinding
import au.com.codeka.podcreep.model.store.Podcast
import au.com.codeka.podcreep.model.store.Subscription
import kotlinx.android.synthetic.main.subscriptions.view.*

class SubscriptionsLayout(
    context: Context,
    private val screen: SubscriptionsScreen,
    private val subscriptionsLiveData: LiveData<List<Subscription>>,
    private var callbacks: Callbacks)
  : RelativeLayout(context) {

  private val adapter: Adapter

  interface Callbacks {
    fun onViewPodcastClick(podcast: LiveData<Podcast>)
  }
  init {
    View.inflate(context, R.layout.subscriptions, this)
    adapter = Adapter(callbacks)
    subscriptions.layoutManager = LinearLayoutManager(context)
    subscriptions.adapter = adapter
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    val data = subscriptionsLiveData.value
    if (data != null) {
      adapter.refresh(data)
    }
    Log.i("DEANH", "observing...")
    subscriptionsLiveData.observe(screen, Observer {
      data -> run {
          Log.i("DEANH", "observer updated, refreshing ($data)")
          adapter.refresh(data)
        }
    })
  }

  class Adapter(private val callbacks: Callbacks)
    : RecyclerView.Adapter<ViewHolder>() {
    var dataset: ArrayList<Subscription> = ArrayList()

    fun refresh(subscriptions: List<Subscription>) {
      dataset.clear()
      dataset.addAll(subscriptions)
      notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
      val inflater = LayoutInflater.from(parent.context)
      val binding = SubscriptionsRowBinding.inflate(inflater, parent, false)
      return ViewHolder(binding, callbacks)
    }

    override fun getItemCount(): Int {
      return dataset.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
      holder.bind(dataset[position])
    }
  }

  class ViewHolder(val binding: SubscriptionsRowBinding, val callbacks: Callbacks)
    : RecyclerView.ViewHolder(binding.root) {

    fun bind(subscription: Subscription) {
      binding.podcast = subscription.podcast.value
      binding.executePendingBindings()
      binding.root.setOnClickListener {
        run {
          callbacks.onViewPodcastClick(subscription.podcast)
        }
      }
    }
  }
}
