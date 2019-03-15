package au.com.codeka.podcreep

import android.app.Application
import android.os.Handler
import au.com.codeka.podcreep.concurrency.TaskRunner
import au.com.codeka.podcreep.concurrency.Threads
import au.com.codeka.podcreep.model.store.Store

class App : Application() {
  companion object {
    lateinit var i: App
  }

  private lateinit var _taskRunner: TaskRunner
  private lateinit var _store: Store

  override fun onCreate() {
    super.onCreate()
    Threads.UI.setThread(Thread.currentThread(), Handler())

    _taskRunner = TaskRunner()
    _store = Store(this, taskRunner)
    i = this
  }

  val taskRunner: TaskRunner
    get() = _taskRunner

  val store: Store
    get() = _store
}
