package com.podcreep.model.store

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.room.Room
import com.podcreep.concurrency.TaskRunner
import com.podcreep.concurrency.Threads

/**
 * Store is a class we use for local storage of sub_podcasts, episode podcast_details, and all that stuff.
 * This class also manages requesting from the server for updates to this data as required. You
 * should use the {@link LiveData} methods to make sure you're always displaying the most up-to-date
 * data.
 */
class Store(applicationContext: Context, private val taskRunner: TaskRunner) {
  /**
   * This is exposed mostly for the sync code to access directly. Don't use it in normal code.
   */
  val localStore: LocalStore = Room
      .databaseBuilder(applicationContext, LocalStore::class.java, "local-store")
      // We'll just allow database upgrades to drop the data store, since it's just a cache of the
      // server anyway, it shouldn't be a big deal.
      .fallbackToDestructiveMigration()
      .build()

  /**
   * Gets a list of the subscriptions the user has subscribed to. We'll also keep the podcast within
   * the subscription updated as well.
   */
  fun subscriptions(): LiveData<List<Subscription>> {
    val subscriptions = localStore.subscriptions().get()

    val converter = MediatorLiveData<List<Subscription>>()
    converter.addSource(subscriptions) {
      taskRunner.runTask(Threads.BACKGROUND) {
        val podcasts = localStore.podcasts().getSync()
        for (s in it) {
          for (p in podcasts) {
            if (s.podcastID == p.id) {
              taskRunner.runTask(Threads.UI) {
                s.podcast.value = p
              }
              break
            }
          }
        }
        taskRunner.runTask(Threads.UI) {
          converter.value = it
        }
      }
    }
    return converter
  }

  /**
   * Fetches a list of in-progress episodes (that is, episodes that we have listened to at least some of).
   */
  fun inProgress(): LiveData<List<Episode>> {
    return localStore.episodes().getInProgress()
  }

  /**
   * Fetches a list of new episodes (that is, episodes that we have not listened to).
   */
  fun newEpisodes(): LiveData<List<Episode>> {
    return localStore.episodes().getNewEpisodes()
  }

  /** Get the podcast with the given ID. */
  fun podcast(id: Long): LiveData<Podcast> {
    return localStore.podcasts().get(id)
  }

  /** Gets a list of the episodes for the given podcast. */
  fun episodes(podcastID: Long): LiveData<List<Episode>> {
    return localStore.episodes().get(podcastID)
  }

  /** Gets a single list from the given podcast. */
  fun episode(podcastID: Long, episodeID: Long): LiveData<Episode> {
    return localStore.episodes().get(podcastID, episodeID)
  }
}
