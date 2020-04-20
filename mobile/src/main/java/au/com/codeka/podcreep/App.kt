package au.com.codeka.podcreep

import android.app.Application
import android.os.Handler
import au.com.codeka.podcreep.concurrency.TaskRunner
import au.com.codeka.podcreep.concurrency.Threads
import au.com.codeka.podcreep.model.cache.EpisodeMediaCache
import au.com.codeka.podcreep.model.cache.PodcastIconCache
import au.com.codeka.podcreep.model.store.Store
import au.com.codeka.podcreep.net.Server

class App : Application() {
  companion object {
    lateinit var i: App
  }

  private lateinit var _taskRunner: TaskRunner
  private lateinit var _store: Store
  private lateinit var _iconCache: PodcastIconCache
  private lateinit var _mediaCache: EpisodeMediaCache

  override fun onCreate() {
    super.onCreate()
    Threads.UI.setThread(Thread.currentThread(), Handler())

    _taskRunner = TaskRunner()
    _store = Store(this, taskRunner)
    _iconCache = PodcastIconCache(this, store, taskRunner)
    _mediaCache = EpisodeMediaCache(this, taskRunner)
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
}
