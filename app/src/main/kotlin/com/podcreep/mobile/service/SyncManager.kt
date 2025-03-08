package com.podcreep.mobile.service

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.DelegatingWorkerFactory
import androidx.work.ListenableWorker
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.podcreep.mobile.Settings
import com.podcreep.mobile.domain.sync.StoreSyncer
import com.podcreep.mobile.util.L
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Date
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * SyncManager handles synchronizing our data store with the server periodically. We use the
 * Jetpack WorkManager class to handle scheduling and running, though you can also run immediately
 * (e.g. if the user clicks 'refresh' in the UI).
 */
@Singleton
class SyncManager @Inject constructor(
  @ApplicationContext val context: Context,
  private val storeSyncer: StoreSyncer,
  private val settings: Settings) {

  private val L: L = L("SyncManager")
  private var isSyncing = false

  /** Actually performs the sync. */
  private suspend fun performSync(): Boolean {
    if (isSyncing) {
      L.info("Already syncing, not starting again")
      return true
    }
    isSyncing = true

    try {
      storeSyncer.sync()
    } catch (e: Exception) {
      L.warning("Error", e)
      return false
    } finally {
      isSyncing = false
    }

    return true
  }

  /** Called at start up to ensure our Worker is enqueued and is going to run. */
  fun maybeEnqueue() {
    val uuid: String = settings.get(Settings.SYNC_WORK_ID)
    if (uuid.isEmpty()) {
      // No saved UUID, meaning it's never been enqueued before.
      enqueueWorker()
      return
    }

    val workInfo = WorkManager.getInstance(context).getWorkInfoById(UUID.fromString(uuid))
    when (workInfo.get().state) {
      WorkInfo.State.CANCELLED, WorkInfo.State.FAILED -> enqueueWorker()
      else -> L.info("Worker already queued, nothing to do.")
    }
  }

  /** If we haven't run in a while, run now. */
  fun maybeSync() {
    val lastSync: LocalDateTime = settings.get(Settings.LAST_SYNC_TIME)

    // If we haven't synced in the last hour, sync now.
    val dontSyncAfter = LocalDateTime.now().plusHours(1)
    if (lastSync.isBefore(dontSyncAfter)) {
      settings.put(Settings.LAST_SYNC_TIME, LocalDateTime.now())

      CoroutineScope(Dispatchers.IO).launch {
        sync()
      }
    }
  }

  /** Perform a sync now. Runs on a background thread. */
  suspend fun sync() {
    performSync()
  }

  /** Called when we log out, we should clear our local data store. */
  suspend fun logout() {
    storeSyncer.logout()
    settings.put(Settings.LAST_SYNC_TIME, LocalDateTime.MIN)
  }

  private fun enqueueWorker() {
    val workRequest = PeriodicWorkRequestBuilder<SyncWorker>(1L, TimeUnit.HOURS)
        .setConstraints(
          Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresStorageNotLow(true)
            .build())
        .build()
    WorkManager.getInstance(context).enqueue(workRequest).result.get()
    L.info(String.format("New worker enqueued, ID: %s", workRequest.id.toString()))

    val s = Settings(context)
    s.put(Settings.SYNC_WORK_ID, workRequest.id.toString())
  }

  // TODO(dean): We might need more than one worker factory, if we end up with more than one type
  // of worker?
  class SyncWorkerFactory @Inject constructor(
    private val syncManager: SyncManager
  ) : WorkerFactory() {
    override fun createWorker(
      appContext: Context,
      workerClassName: String,
      params: WorkerParameters
    ): ListenableWorker? {
      return when (workerClassName) {
        SyncWorker::class.java.name -> SyncWorker(appContext, params, syncManager)
        else -> null
      }
    }
  }

  class SyncDelegatingWorkerFactory @Inject constructor(
    private val syncWorkerFactory: SyncWorkerFactory
  ) : DelegatingWorkerFactory() {
    init {
      addFactory(syncWorkerFactory)
    }
  }

  class SyncWorker(
    context: Context,
    params: WorkerParameters,
    private val syncManager: SyncManager) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
      return when(syncManager.performSync()) {
        true -> Result.success()
        false -> Result.retry()
      }
    }
  }
}
