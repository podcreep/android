package com.podcreep.model.cache

import android.content.Context
import android.net.Uri
import android.util.Log
import com.podcreep.app.service.MediaIdBuilder
import com.podcreep.concurrency.TaskRunner
import com.podcreep.concurrency.Threads
import com.podcreep.model.store.Episode
import com.podcreep.model.store.Podcast
import com.podcreep.net.HttpRequest
import java.io.File

/**
 * This is the episode cache. You can download the media file for a podcast episode and then play
 * it back from here directly. This allows us offline access to our podcasts.
 */
class EpisodeMediaCache(private val appContext: Context, private val taskRunner: TaskRunner) {
  companion object {
    private const val TAG = "EpisodeMediaCache"
  }

  /** The download status of this episode's media. */
  enum class Status {
    /** The episode has not been downloaded and is not in-progress either. */
    NotDownloaded,

    /** The download of the episode is in-progress. */
    InProgress,

    /** The episode is fully-downloaded and on disk. */
    Downloaded
  }

  private val inProgressDownloads = HashMap<String, InProgressDownload>()

  /** Get the download-status of the given episode. */
  fun getStatus(podcast: Podcast, episode: Episode): Status {
    val mediaId = MediaIdBuilder().getMediaId(podcast, episode)
    if (inProgressDownloads.containsKey(mediaId)) {
      return Status.InProgress
    } else {
      val file = cacheFile(podcast, episode)
      if (file.exists()) {
        return Status.Downloaded
      }
    }

    return Status.NotDownloaded
  }

  /**
   * Gets the Uri for playing the episode from the given file. Returns null if the file hasn't
   * been downloaded.
   */
  fun getUri(podcast: Podcast, episode: Episode): Uri? {
    val file = cacheFile(podcast, episode)
    if (file.exists()) {
      return Uri.fromFile(file)
    }

    return null
  }

  fun download(podcast: Podcast, episode: Episode) {
    taskRunner.runTask({
      Log.i(TAG, "Downloading media for '${podcast.title}' episode '${episode.title}'...")
      val mediaId = MediaIdBuilder().getMediaId(podcast, episode)
      val ipd = InProgressDownload(podcast, episode)
      inProgressDownloads[mediaId] = ipd

      val startTime = System.currentTimeMillis()
      val req = HttpRequest.Builder()
          .url(episode.mediaUrl)
          .method(HttpRequest.Method.GET)
          .build()
      val resp = req.execute()
      Log.i(TAG, " - ${req.contentSize} bytes total after ${System.currentTimeMillis() - startTime}ms")
      ipd.totalSize = req.contentSize

      val ins = resp.inputStream()
      val outs = cacheFile(podcast, episode).outputStream()
      val buffer = ByteArray(1024)
      do {
        val n = ins.read(buffer)
        if (n == 0) {
          break
        }

        outs.write(buffer, 0, n)
        ipd.currDownloaded += n
      } while (true)

      inProgressDownloads.remove(mediaId)
    }, Threads.BACKGROUND)
  }

  private fun cacheFile(podcast: Podcast, episode: Episode): File {
    // Doesn't have to be an .mp3 file, but we'll use that extension just for fun.
    val file = File(appContext.cacheDir, "podcasts/${podcast.id}/${episode.id}-media.mp3")
    file.parentFile?.mkdirs()
    return file
  }

  data class InProgressDownload (val podcast: Podcast, val episode: Episode) {
    var totalSize: Long = 0
    var currDownloaded: Long = 0
  }
}