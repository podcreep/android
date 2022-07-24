package com.podcreep.concurrency

import android.util.Log
import java.util.*

/**
 * This is a class for running tasks on various threads. You can run a task on any thread defined
 * in [Threads].
 */
class TaskRunner {
  private val backgroundThreadPool: ThreadPool = ThreadPool(
      Threads.BACKGROUND,
      750 /* maxQueuedItems */,
      5 /* minThreads */,
      20 /* maxThreads */,
      1000 /* keepAliveMs */)
  private val timer: Timer

  init {
    Threads.BACKGROUND.setThreadPool(backgroundThreadPool)

    timer = Timer("Timer")
  }

  inline fun <reified E : Throwable> runTask(
    thread: Threads, crossinline errorHandler: (E) -> Unit, crossinline task: () -> Unit) {

    thread.runTask {
      try {
        task()
      } catch (e: Throwable) {
        if (e is E) {
          errorHandler(e)
        } else {
          Log.e("TaskRunner", "UnhandledException", e)
        }
      }
    }
  }

  fun runTask(task: () -> Unit, thread: Threads) {
    runTask(thread, { e: Throwable -> Log.e("TaskRunner", "UnhandledException", e) }, task)
  }

  fun runTask(runnable: Runnable, thread: Threads) {
    thread.runTask(runnable)
  }

  /** Run a task after the given delay.  */
  fun runTask(runnable: Runnable, thread: Threads, delayMs: Long) {
    if (delayMs == 0L) {
      runTask(runnable, thread)
    } else {
      timer.schedule(object : TimerTask() {
        override fun run() {
          runTask(runnable, thread)
        }
      }, delayMs)
    }
  }
}