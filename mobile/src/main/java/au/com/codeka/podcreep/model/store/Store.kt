package au.com.codeka.podcreep.model.store

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.room.Room
import au.com.codeka.podcreep.concurrency.TaskRunner
import au.com.codeka.podcreep.concurrency.Threads
import au.com.codeka.podcreep.model.sync.PodcastOld
import au.com.codeka.podcreep.model.sync.SubscriptionOld
import au.com.codeka.podcreep.model.sync.toEntity
import au.com.codeka.podcreep.model.sync.SubscriptionListOld
import au.com.codeka.podcreep.net.HttpRequest
import au.com.codeka.podcreep.net.Server

/**
 * Store is a class we use for local storage of subscriptions, episode details, and all that stuff.
 * This class also manages requesting from the server for updates to this data as required. You
 * should use the {@link LiveData} methods to make sure you're always displaying the most up-to-date
 * data.
 */
class Store(applicationContext: Context, private val taskRunner: TaskRunner) {
  private val localStore: LocalStore = Room
      .databaseBuilder(applicationContext, LocalStore::class.java, "local-store")
      // We'll just allow database upgrades to drop the data store, since it's just a cache of the server
      // anyway, it shouldn't be a big deal.
      .fallbackToDestructiveMigration()
      .build()

  /**
   * Gets a list of the subscriptions the user has subscribed to. We'll also keep the podcast within the subscription
   * updated as well.
   */
  fun subscriptions(): LiveData<List<Subscription>> {
    val subscriptions = localStore.subscriptions().get()

    val converter = MediatorLiveData<List<Subscription>>()
    converter.addSource(subscriptions) {
      taskRunner.runTask({
        val podcasts = localStore.podcasts().getSync()
        for (s in it) {
          for (p in podcasts) {
            if (s.podcastID == p.id) {
              s.podcast = p
              break
            }
          }
        }
        taskRunner.runTask({
          converter.value = it
        }, Threads.UI)
      }, Threads.BACKGROUND)
    }
    return converter
  }
}
