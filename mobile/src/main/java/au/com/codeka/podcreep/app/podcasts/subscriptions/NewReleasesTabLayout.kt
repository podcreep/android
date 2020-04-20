package au.com.codeka.podcreep.app.podcasts.subscriptions

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import au.com.codeka.podcreep.concurrency.TaskRunner
import au.com.codeka.podcreep.model.store.Store

class NewReleasesTabLayout(
    context: Context,
    private val lifecyleOwner: LifecycleOwner,
    private val store: Store,
    private val taskRunner: TaskRunner,
    private val callbacks: SubscriptionsLayout.Callbacks)
  : BaseEpisodeListTabLayout(context, lifecyleOwner, store.subscriptions(), store.newEpisodes(),
                             callbacks)
