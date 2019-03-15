package au.com.codeka.podcreep.model.store

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.room.Room
import au.com.codeka.podcreep.concurrency.TaskRunner
import au.com.codeka.podcreep.concurrency.Threads
import au.com.codeka.podcreep.model.Subscription
import au.com.codeka.podcreep.model.toEntity
import au.com.codeka.podcreep.model.SubscriptionList
import au.com.codeka.podcreep.net.HttpRequest
import au.com.codeka.podcreep.net.Server
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.migration.Migration



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
   * Gets a list of the subscriptions the user has subscribed to.
   */
  fun subscriptions(): LiveData<List<Subscription>> {
    val subscriptions = localStore.subscriptions().get()

    taskRunner.runTask({
      val request = Server.request("/api/subscriptions")
          .method(HttpRequest.Method.GET)
          .build()
      val resp = request.execute<SubscriptionList>()
      val podcasts = ArrayList<PodcastEntity>()
      for (s in resp.subscriptions) {
        val p = s.podcast
        if (p != null) {
          podcasts.add(s.podcast.toEntity())
        }
      }
      localStore.podcasts().insert(*podcasts.toTypedArray())
      localStore.subscriptions().updateAll(resp.subscriptions.toEntity())
    }, Threads.BACKGROUND)

    val converter = MediatorLiveData<List<Subscription>>()
    converter.addSource(subscriptions) { Subscription.fromEntity(it) }
    return converter
  }
}
