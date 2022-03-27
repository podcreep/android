package com.podcreep.app.podcasts.subscriptions

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import com.podcreep.app.podcasts.episode.BaseEpisodeListLayout
import com.podcreep.concurrency.TaskRunner
import com.podcreep.model.store.Store

class InProgressTabLayout(
    context: Context, lifecycleOwner: LifecycleOwner, store: Store, taskRunner: TaskRunner,
    callbacks: SubscriptionsLayout.Callbacks)
  : BaseEpisodeListLayout(
        context, lifecycleOwner, store.subscriptions(), store.inProgress(), callbacks)
