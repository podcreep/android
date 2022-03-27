package com.podcreep.concurrency

import android.util.Log
import java.util.*

/**
 * This is a class for running tasks on various threads. You can run a task on any thread defined
 * in [Threads].
 */
class TaskRunner {
  private val backgroundThreadPool: ThreadPool
  private val timer: Timer

  init {
    backgroundThreadPool = ThreadPool(
        Threads.BACKGROUND,
        750 /* maxQueuedItems */,
        5 /* minThreads */,
        20 /* maxThreads */,
        1000 /* keepAliveMs */)
    Threads.BACKGROUND.setThreadPool(backgroundThreadPool)

    timer = Timer("Timer")
  }

  fun runTask(task: () -> Unit, thread: Threads) {
    thread.runTask(Runnable {
      try {
        task()
      } catch(e: Throwable) {
        Log.e("TaskRunner", "Unhandled exception", e)
      }
    })
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