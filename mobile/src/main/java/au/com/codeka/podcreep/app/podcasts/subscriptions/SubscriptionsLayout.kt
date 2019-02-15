package au.com.codeka.podcreep.app.podcasts.subscriptions

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import au.com.codeka.podcreep.R
import au.com.codeka.podcreep.concurrency.TaskRunner
import au.com.codeka.podcreep.databinding.SubscriptionsRowBinding
import au.com.codeka.podcreep.model.Podcast
import au.com.codeka.podcreep.model.Subscription
import kotlinx.android.synthetic.main.subscriptions.view.*

class SubscriptionsLayout(
    context: Context,
    taskRunner: TaskRunner,
    var callbacks: Callbacks)
  : RelativeLayout(context) {

  private val adapter: Adapter

  interface Callbacks {
    fun onViewPodcastClick(podcast: Podcast)
  }
  init {
    View.inflate(context, R.layout.subscriptions, this)
    adapter = Adapter(callbacks)
    subscriptions.layoutManager = LinearLayoutManager(context)
    subscriptions.adapter = adapter
  }

  fun refresh(subscriptions: List<Subscription>) {
    adapter.refresh(subscriptions)
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
      binding.podcast = subscription.podcast
      binding.executePendingBindings()
      binding.root.setOnClickListener {
        run {
          callbacks.onViewPodcastClick(subscription.podcast!!)
        }
      }
    }
  }
}
