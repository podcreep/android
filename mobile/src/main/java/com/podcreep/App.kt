package com.podcreep

import android.app.Application
import android.os.Handler
import android.os.Looper
import com.podcreep.app.service.MediaServiceClient
import com.podcreep.app.service.SyncManager
import com.podcreep.concurrency.TaskRunner
import com.podcreep.concurrency.Threads
import com.podcreep.model.cache.EpisodeMediaCache
import com.podcreep.model.cache.PodcastIconCache
import com.podcreep.model.store.Store
import com.podcreep.net.Server

class App : Application() {
  companion object {
    lateinit var i: App
  }

  private lateinit var _taskRunner: TaskRunner
  val taskRunner: TaskRunner
    get() = _taskRunner

  private lateinit var _store: Store
  val store: Store
    get() = _store

  private lateinit var _iconCache: PodcastIconCache
  val iconCache: PodcastIconCache
    get() = _iconCache

  private lateinit var _mediaCache: EpisodeMediaCache
  val mediaCache: EpisodeMediaCache
    get() = _mediaCache

  private lateinit var _syncManager: SyncManager
  val syncManager: SyncManager
    get() = _syncManager

  private lateinit var _mediaServiceClient: MediaServiceClient
  val mediaServiceClient: MediaServiceClient
    get() = _mediaServiceClient

  override fun onCreate() {
    super.onCreate()
    Threads.UI.setThread(Thread.currentThread(), Handler(Looper.getMainLooper()))

    _taskRunner = TaskRunner()
    _store = Store(this, taskRunner)
    _iconCache = PodcastIconCache(this, store, taskRunner)
    _mediaCache = EpisodeMediaCache(this, taskRunner)
    _syncManager = SyncManager(this, taskRunner)
    _mediaServiceClient = MediaServiceClient(this)
    i = this

    val s = Settings(this)
    if (s.getString(Settings.COOKIE) != "") {
      Server.updateCookie(s.getString(Settings.COOKIE))
    }
  }
}
