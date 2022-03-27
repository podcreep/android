package com.podcreep

import android.app.Application
import android.os.Handler
import android.os.Looper
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
  private lateinit var _store: Store
  private lateinit var _iconCache: PodcastIconCache
  private lateinit var _mediaCache: EpisodeMediaCache
  private lateinit var _syncManager: SyncManager

  override fun onCreate() {
    super.onCreate()
    Threads.UI.setThread(Thread.currentThread(), Handler(Looper.getMainLooper()))

    _taskRunner = TaskRunner()
    _store = Store(this, taskRunner)
    _iconCache = PodcastIconCache(this, store, taskRunner)
    _mediaCache = EpisodeMediaCache(this, taskRunner)
    _syncManager = SyncManager(this, taskRunner)
    i = this

    val s = Settings(this)
    if (s.getString(Settings.COOKIE) != "") {
      Server.updateCookie(s.getString(Settings.COOKIE))
    }
  }

  val taskRunner: TaskRunner
    get() = _taskRunner

  val store: Store
    get() = _store

  val iconCache: PodcastIconCache
    get() = _iconCache

  val mediaCache: EpisodeMediaCache
    get() = _mediaCache

  val syncManager: SyncManager
    get() = _syncManager
}
