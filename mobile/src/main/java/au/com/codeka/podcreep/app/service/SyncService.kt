package au.com.codeka.podcreep.app.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import au.com.codeka.podcreep.App
import au.com.codeka.podcreep.concurrency.TaskRunner
import au.com.codeka.podcreep.concurrency.Threads
import au.com.codeka.podcreep.model.sync.*
import au.com.codeka.podcreep.net.HttpException
import au.com.codeka.podcreep.net.HttpRequest
import au.com.codeka.podcreep.net.Server
import java.lang.Exception

/**
 * SyncService's job is to synchronize the local database with the server. We'll also notify the
 * user of any new episodes for podcasts they've asked to be notified about, and download the media
 * file for any as well.
 */
class SyncService : Service() {
  private lateinit var taskRunner: TaskRunner
  private lateinit var notificationManager: NotificationManager

  companion object {
    const val TAG = "SyncService"
  }

  override fun onBind(p0: Intent?): IBinder? {
    // No binder, we're single-purpose.
    return null
  }

  override fun onCreate() {
    super.onCreate()

    taskRunner = App.i.taskRunner
    notificationManager = NotificationManager(this, 1235, "refresh", "Refreshing service")
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    val id = super.onStartCommand(intent, flags, startId)

    notificationManager.refresh("Refreshing...")
    notificationManager.startForeground()

    val syncer = StoreSyncer(App.i.store)

    taskRunner.runTask({
      val request = Server.request("/api/subscriptions/sync")
          .method(HttpRequest.Method.POST)
          .body(SubscriptionsSyncPostRequest(false))
          .build()
      try {
        val resp = request.execute<SubscriptionsSyncPostResponse>()
        syncer.sync(resp)
      } catch (e: HttpException) {
        Log.e(TAG, "Error", e)
        notifyToast(e.message!!)
      } catch (e: Exception) {
        Log.e(TAG, "Error", e)
        notifyToast(e.toString())
      }

      taskRunner.runTask({
        notificationManager.stopService()
      }, Threads.UI)
    }, Threads.BACKGROUND)

    return id
  }

  private fun notifyToast(msg: String) {
    taskRunner.runTask({
      Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }, Threads.UI)
  }
}
