package com.podcreep.mobile.domain.cache

import android.content.Context
import android.net.Uri
import com.podcreep.mobile.data.local.Episode
import com.podcreep.mobile.data.local.Podcast
import com.podcreep.mobile.util.L
import com.podcreep.mobile.util.Server
import com.podcreep.mobile.util.await
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import okhttp3.internal.headersContentLength
import okio.buffer
import okio.sink
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * This is the episode cache. You can download the media file for a podcast episode and then play it
 * back from here directly. This allows us offline access to our podcasts.
 */
@Singleton
class EpisodeMediaCache @Inject constructor(
  @param:ApplicationContext val appContext: Context,
  private val server: Server) {

  private val L = L("EpisodeMediaCache")

  /** The download status of this episode's media. */
  enum class Status {
    /** The episode has not been downloaded and is not in-progress either. */
    NotDownloaded,

    /** The download of the episode is in-progress. */
    InProgress,

    /** The episode is fully-downloaded and on disk. */
    Downloaded,

    /** There was an error downloading the episode. You can probably try again. */
    DownloadFailed,
  }

  data class DownloadProgress(
    val status: Status,
    val progressPercent: Int? = null
  )

  private val inProgressDownloads = HashMap<String, InProgressDownload>()
  private val failedDownloads = HashSet<String>()
  private val _statusUpdates = MutableSharedFlow<String>(
    extraBufferCapacity = 64,
    onBufferOverflow = BufferOverflow.DROP_OLDEST
  )

  private fun mediaKey(podcast: Podcast, episode: Episode): String {
    return "${podcast.id}:${episode.id}"
  }

  private fun notifyStatusChanged(key: String) {
    _statusUpdates.tryEmit(key)
  }

  /** Get the download-status of the given episode. */
  fun getStatus(podcast: Podcast, episode: Episode): Status {
    return getProgress(podcast, episode).status
  }

  fun getProgress(podcast: Podcast, episode: Episode): DownloadProgress {
    val key = mediaKey(podcast, episode)
    val ipd = inProgressDownloads[key]
    if (ipd != null) {
      val percent = if (ipd.totalSize > 0) {
        ((ipd.currDownloaded * 100) / ipd.totalSize).toInt()
      } else null
      return DownloadProgress(Status.InProgress, percent)
    }
    val file = cacheFile(podcast, episode)
    if (file.exists()) {
      return DownloadProgress(Status.Downloaded)
    }
    if (failedDownloads.contains(key)) {
      return DownloadProgress(Status.DownloadFailed)
    }
    return DownloadProgress(Status.NotDownloaded)
  }

  fun observeProgress(podcast: Podcast, episode: Episode): Flow<DownloadProgress> = flow {
    val key = mediaKey(podcast, episode)
    emit(getProgress(podcast, episode))
    _statusUpdates.collect { updatedKey ->
      if (updatedKey == key) {
        emit(getProgress(podcast, episode))
      }
    }
  }

  /**
   * Gets the Uri for playing the episode from the given file. Returns null if the file hasn't been
   * downloaded or started downloading.
   */
  fun getUri(podcast: Podcast, episode: Episode): Uri? {
    val file = cacheFile(podcast, episode)
    if (file.exists()) {
      return Uri.fromFile(file)
    }

    return null
  }

  fun queueDownload(podcast: Podcast, episode: Episode) {
    CoroutineScope(Dispatchers.IO).launch {
      download(podcast, episode)
    }
  }

  suspend fun download(podcast: Podcast, episode: Episode) {
    val key = mediaKey(podcast, episode)
    if (inProgressDownloads.containsKey(key)) {
      L.info("Download already in progress for $key")
      return
    }
    val file = cacheFile(podcast, episode)
    if (file.exists() && getStatus(podcast, episode) == Status.Downloaded) {
      L.info("Already downloaded for $key")
      return
    }

    L.info("Downloading media for '${podcast.title}' episode '${episode.title}'...")
    failedDownloads.remove(key)
    val ipd = InProgressDownload(podcast, episode)
    inProgressDownloads[key] = ipd
    notifyStatusChanged(key)

    try {
      val startTime = System.currentTimeMillis()
      val req = server.request(episode.mediaUrl).get()
      val resp = server.call(req).await()
      L.info(" - ${resp.headersContentLength()} bytes total after ${System.currentTimeMillis() - startTime}ms")
      ipd.totalSize = resp.headersContentLength()

      var lastPercent = -1

      resp.body?.source()?.use { ins ->
        file.sink().buffer().use { outs ->
          val buffer = ByteArray(8192)
          while (true) {
            val n = ins.read(buffer)
            if (n <= 0) break
            outs.write(buffer, 0, n)
            outs.flush()
            ipd.currDownloaded += n

            val percent = if (ipd.totalSize > 0) {
              ((ipd.currDownloaded * 100) / ipd.totalSize).toInt()
            } else -1
            if (percent != lastPercent) {
              lastPercent = percent
              notifyStatusChanged(key)
            }
          }
        }
      }
    } catch (e: Exception) {
      L.warning("Failed to download episode ${episode.id}", e)
      failedDownloads.add(key)
      if (file.exists()) {
        file.delete()
      }
    } finally {
      inProgressDownloads.remove(key)
      notifyStatusChanged(key)
    }
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
