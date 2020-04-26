package au.com.codeka.podcreep.app.service

import android.content.Context
import android.util.Log
import androidx.work.*
import au.com.codeka.podcreep.App
import au.com.codeka.podcreep.Settings
import au.com.codeka.podcreep.concurrency.TaskRunner
import au.com.codeka.podcreep.concurrency.Threads
import au.com.codeka.podcreep.model.cache.PodcastIconCache
import au.com.codeka.podcreep.model.sync.StoreSyncer
import au.com.codeka.podcreep.net.HttpException
import java.lang.Exception
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * SyncManager handles synchronizing our data store with the server periodically. We use the
 * Jetpack WorkManager class to handle scheduling and running, though you can also run immediately
 * (e.g. if the user clicks 'refresh' in the UI).
 */
class SyncManager(private val context: Context, private val taskRunner: TaskRunner) {
  companion object {
    const val TAG = "SyncManager"

    /** Actually performs the sync, assumes we're running on some kind of background thread. */
    private fun performSync(context: Context): Boolean {
      val syncer = StoreSyncer(context, App.i.store, App.i.iconCache)

      try {
        syncer.sync()
      } catch (e: HttpException) {
        Log.e(TAG, "Error", e)
        return false
      } catch (e: Exception) {
        Log.e(TAG, "Error", e)
        return false
      }

      return true
    }
  }

  /**
   * Called at start up to ensure our Worker is enqueued and is going to run.
   */
  fun maybeEnqueue() {
    taskRunner.runTask({
      val s = Settings(context)
      val uuid: String = s.get(Settings.SYNC_WORK_ID)
      if (uuid.isEmpty()) {
        // No saved UUID, meaning it's never been enqueued before.
        enqueueWorker()
        return@runTask
      }

      val workInfo = WorkManager.getInstance(context).getWorkInfoById(UUID.fromString(uuid))
      when (workInfo.get().state) {
        WorkInfo.State.CANCELLED, WorkInfo.State.FAILED -> enqueueWorker()
        else -> Log.i(TAG, "Worker already queued, nothing to do.")
      }
    }, Threads.BACKGROUND)
  }

  /** If we haven't run in a while, run now. */
  fun maybeSync() {
    synchronized(this) {
      val s = Settings(context)
      val lastSync: Date = s.get(Settings.LAST_SYNC_TIME)

      // If we haven't synced in the last hour, sync now.
      val dontSyncAfter = Date(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1))
      if (lastSync.before(dontSyncAfter)) {
        s.put(Settings.LAST_SYNC_TIME, Date())

        sync()
      }
    }
  }

  /** Perform a sync now. Runs on a background thread. */
  fun sync() {
    taskRunner.runTask({
      performSync(context)
    }, Threads.BACKGROUND)
  }

  private fun enqueueWorker() {
    val workRequest = PeriodicWorkRequestBuilder<SyncWorker>(1, TimeUnit.HOURS)
        .setConstraints(Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresStorageNotLow(true)
            .build())
        .build()
    WorkManager.getInstance(context).enqueue(workRequest).result.get()
    Log.i(TAG, String.format("New worker enqueued, ID: %s", workRequest.id.toString()))

    val s = Settings(context)
    s.put(Settings.SYNC_WORK_ID, workRequest.id.toString())
  }

  class SyncWorker(private val context: Context, params: WorkerParameters)
    : Worker(context, params) {
    override fun doWork(): Result {
      return when(performSync(context)) {
        true -> Result.success()
        false -> Result.retry()
      }
    }
  }
}