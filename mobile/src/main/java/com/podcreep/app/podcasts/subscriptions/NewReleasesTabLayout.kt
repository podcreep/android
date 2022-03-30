package com.podcreep.app.podcasts.subscriptions

import android.content.Context
import androidx.annotation.Keep
import androidx.lifecycle.LifecycleOwner
import com.podcreep.app.podcasts.episode.BaseEpisodeListLayout
import com.podcreep.concurrency.TaskRunner
import com.podcreep.model.store.Store

class NewReleasesTabLayout @Keep constructor(
    context: Context, lifecycleOwner: LifecycleOwner, store: Store, taskRunner: TaskRunner,
    callbacks: SubscriptionsLayout.Callbacks)
  : BaseEpisodeListLayout(context, lifecycleOwner, store.subscriptions(), store.newEpisodes(),
                             callbacks)
