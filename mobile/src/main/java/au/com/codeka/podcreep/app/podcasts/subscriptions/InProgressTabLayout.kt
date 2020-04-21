package au.com.codeka.podcreep.app.podcasts.subscriptions

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import au.com.codeka.podcreep.app.podcasts.episode.BaseEpisodeListLayout
import au.com.codeka.podcreep.concurrency.TaskRunner
import au.com.codeka.podcreep.model.store.Store

class InProgressTabLayout(
    context: Context, lifecycleOwner: LifecycleOwner, store: Store, taskRunner: TaskRunner,
    callbacks: SubscriptionsLayout.Callbacks)
  : BaseEpisodeListLayout(
        context, lifecycleOwner, store.subscriptions(), store.inProgress(), callbacks)

