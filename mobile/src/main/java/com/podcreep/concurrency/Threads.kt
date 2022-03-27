package com.podcreep.concurrency

import android.os.Handler

/**
 * An enumeration of the thread types within War Worlds. Has some helper methods to ensure you run
 * on a particular thread.
 */
enum class Threads {
  /**
   * The main UI thread.
   */
  UI,

  /**
   * The OpenGL render thread. We assume there is only one of these in the whole process.
   */
  GL,

  /**
   * A special "class" of thread that actually represents a pool of background workers.
   */
  BACKGROUND;

  private var isInitialized: Boolean = false
  private var handler: Handler? = null
  private var taskQueue: TaskQueue? = null
  private var thread: Thread? = null
  private var threadPool: ThreadPool? = null

  val isCurrentThread: Boolean
    get() {
      //checkState(isInitialized)

      return when {
        thread != null -> thread === Thread.currentThread()
        threadPool != null -> threadPool!!.isThread(this)
        else -> throw IllegalStateException("thread is null and threadPool is null")
      }
    }

  fun setThread(thread: Thread, taskQueue: TaskQueue) {
    //checkState(!isInitialized || this.taskQueue === taskQueue)
    this.thread = thread
    this.taskQueue = taskQueue
    this.isInitialized = true
  }

  fun setThread(thread: Thread, handler: Handler) {
    //checkState(!isInitialized)
    this.thread = thread
    this.handler = handler
    this.isInitialized = true
  }

  /** Reset this thread, unassociate it from the current thread, handler, task queue combo.  */
  fun resetThread() {
    thread = null
    handler = null
    taskQueue = null
    isInitialized = false
  }

  fun setThreadPool(threadPool: ThreadPool) {
    //checkState(!isInitialized)
    this.threadPool = threadPool
    this.isInitialized = true
  }

  fun runTask(runnable: Runnable) {
    when {
      handler != null -> handler!!.post(runnable)
      threadPool != null -> threadPool!!.runTask(runnable)
      taskQueue != null -> taskQueue!!.postTask(runnable)
      else -> throw IllegalStateException("Cannot run task, no handler, taskQueue or threadPool!")
    }
  }

  companion object {

    fun checkOnThread(thread: Threads) {
      // Note: We don't use Preconditions.checkState because we want a nice error message and don't
      // want to allocate the string for the message every time.
      if (!thread.isCurrentThread) {
        throw IllegalStateException("Unexpectedly not on $thread")
      }
    }

    fun checkNotOnThread(thread: Threads) {
      if (thread.isCurrentThread) {
        throw IllegalStateException("Unexpectedly on $thread")
      }
    }
  }
}
