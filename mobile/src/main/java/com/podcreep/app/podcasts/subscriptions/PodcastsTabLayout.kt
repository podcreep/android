package com.podcreep.app.podcasts.subscriptions

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.annotation.Keep
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.podcreep.App
import com.podcreep.R
import com.podcreep.concurrency.TaskRunner
import com.podcreep.databinding.SubPodcastsRowBinding
import com.podcreep.model.store.Store
import com.podcreep.model.store.Subscription
import kotlinx.android.synthetic.main.sub_podcasts.view.*

class PodcastsTabLayout @Keep constructor(
    context: Context,
    private val lifecyleOwner: LifecycleOwner,
    private val store: Store,
    private val taskRunner: TaskRunner,
    private val callbacks: SubscriptionsLayout.Callbacks)
  : RelativeLayout(context) {

  private val adapter: Adapter
  private val subscriptionsLiveData: LiveData<List<Subscription>>

  init {
    View.inflate(context, R.layout.sub_podcasts, this)
    subscriptionsLiveData = store.subscriptions()

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
    subscriptionsLiveData.observe(lifecyleOwner, Observer {
      data -> run {
      adapter.refresh(data)
    }
    })
  }

  class Adapter(private val callbacks: SubscriptionsLayout.Callbacks)
    : RecyclerView.Adapter<ViewHolder>() {
    private var dataset: ArrayList<Subscription> = ArrayList()

    fun refresh(subscriptions: List<Subscription>) {
      dataset.clear()
      dataset.addAll(subscriptions)
      notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
      val inflater = LayoutInflater.from(parent.context)
      val binding = SubPodcastsRowBinding.inflate(inflater, parent, false)
      return ViewHolder(binding, callbacks)
    }

    override fun getItemCount(): Int {
      return dataset.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
      holder.bind(dataset[position])
    }
  }

  class ViewHolder(
      val binding: SubPodcastsRowBinding, val callbacks: SubscriptionsLayout.Callbacks)
    : RecyclerView.ViewHolder(binding.root) {

    fun bind(subscription: Subscription) {
      binding.podcast = subscription.podcast.value
      binding.iconCache = App.i.iconCache
      binding.executePendingBindings()
      binding.root.setOnClickListener {
        run {
          callbacks.onViewPodcastClick(subscription.podcast)
        }
      }
    }
  }
}
