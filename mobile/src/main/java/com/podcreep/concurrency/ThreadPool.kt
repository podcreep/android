package com.podcreep.concurrency

import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger

/**
 * A pool of threads that we use for running things on [Threads.BACKGROUND].
 */
class ThreadPool(
    private val thread: Threads,
    maxQueuedItems: Int,
    minThreads: Int,
    maxThreads: Int,
    keepAliveMs: Long) {
  private val executor: Executor

  init {
    val threadFactory = object : ThreadFactory {
      private val count = AtomicInteger(1)
      override fun newThread(r: Runnable): Thread {
        return Thread(r, thread.toString() + " #" + count.getAndIncrement())
      }
    }

    val workQueue = LinkedBlockingQueue<Runnable>(maxQueuedItems)

    executor = ThreadPoolExecutor(
        minThreads, maxThreads, keepAliveMs, TimeUnit.MILLISECONDS, workQueue, threadFactory)
  }

  fun runTask(runnable: Runnable) {
    executor.execute(runnable)
  }

  fun isThread(thread: Threads): Boolean {
    return thread === this.thread
  }
}