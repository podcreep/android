package com.podcreep.mobile.domain.cache

import android.content.Context
import android.net.Uri
import com.podcreep.mobile.data.local.Episode
import com.podcreep.mobile.data.local.Podcast
import com.podcreep.mobile.service.MediaIdBuilder
import com.podcreep.mobile.util.L
import com.podcreep.mobile.util.Server
import com.podcreep.mobile.util.await
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.internal.headersContentLength
import okio.buffer
import okio.sink
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * This is the episode cache. You can download the media file for a podcast episode and then play it back from here
 * directly. This allows us offline access to our podcasts.
 */
@Singleton
class EpisodeMediaCache @Inject constructor(
  @ApplicationContext val appContext: Context, private val server: Server) {

  private final val L = L("EpisodeMediaCache")

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
   * Gets the Uri for playing the episode from the given file. Returns null if the file hasn't been downloaded.
   */
  fun getUri(podcast: Podcast, episode: Episode): Uri? {
    val file = cacheFile(podcast, episode)
    if (file.exists()) {
      return Uri.fromFile(file)
    }

    return null
  }

  suspend fun download(podcast: Podcast, episode: Episode) {
    L.info("Downloading media for '${podcast.title}' episode '${episode.title}'...")
    val mediaId = MediaIdBuilder().getMediaId(podcast, episode)
    val ipd = InProgressDownload(podcast, episode)
    inProgressDownloads[mediaId] = ipd

    val startTime = System.currentTimeMillis()
    val req = server.request(episode.mediaUrl)
        .get()
    val resp = server.call(req).await()
    L.info(" - ${resp.headersContentLength()} bytes total after ${System.currentTimeMillis() - startTime}ms")
    ipd.totalSize = resp.headersContentLength()

    val ins = resp.body?.source()

    val outs = cacheFile(podcast, episode).sink().buffer()
    val buffer = ByteArray(1024)
    do {
      val n = ins?.read(buffer)
      if (n == null || n == 0) {
        break
      }

      outs.write(buffer, 0, n)
      ipd.currDownloaded += n
    } while (true)

    inProgressDownloads.remove(mediaId)
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
