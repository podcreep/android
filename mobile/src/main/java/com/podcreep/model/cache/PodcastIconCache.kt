package com.podcreep.model.cache

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.podcreep.BuildConfig
import com.podcreep.concurrency.TaskRunner
import com.podcreep.concurrency.Threads
import com.podcreep.model.store.Podcast
import com.podcreep.model.store.Store
import com.podcreep.net.HttpRequest
import com.podcreep.net.Server
import java.io.File

/**
 * A cache of the podcast icons. We'll save them to disk and hand out Uris that we can display
 * in the app and/or Android Auto.
 */
class PodcastIconCache(private val appContext: Context, private val store: Store,
                       private val taskRunner: TaskRunner) {
  companion object {
    const val TAG = "PodcastIconCache"

    const val MAX_SIZE = 256
  }

  private val connectedPackages = HashSet<String>()

  /**
   * Called when a package connects to our media service. We'll add it to a list of packages we'll allow to access our
   * remote Uris.
   *
   * Note: currently there's no way to remove packages.
   */
  fun onPackageConnected(packageName: String) {
    connectedPackages.add(packageName)
  }

  /** Get a {@link Uri} that can be called from remote processes (such as Android Auto). */
  fun getRemoteUri(podcast: Podcast): Uri {
    val file = cacheFile(podcast)
    if (!file.exists()) {
      refresh(podcast)
    }

    val uri = FileProvider.getUriForFile(appContext, "com.podcreep.fileprovider", file)
    // Grant everybody who has connected to us access to our URI.
    for (packageName in connectedPackages) {
      appContext.grantUriPermission(
        packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    return uri
  }

  /** Get a {@link Uri} that can be called only from this process (e.g. our UI). */
  fun getLocalUri(podcast: Podcast): Uri {
    val file = cacheFile(podcast)
    if (!file.exists()) {
      refresh(podcast)
    }

    return Uri.fromFile(file)
  }

  /** Refresh the cached icon of the given {@link Podcast}. */
  fun refresh(podcast: Podcast) {
    taskRunner.runTask(Threads.BACKGROUND) {
      Log.i(TAG, "Downloading icon for '${podcast.title}'")
      var url = podcast.imageUrl
      if (url.startsWith("/")) {
        url = Server.url(url)
      }
      url += "?width=${MAX_SIZE}&height=${MAX_SIZE}"
      val req = HttpRequest.Builder()
          .url(url) // TODO: grab a few different sizes?
          .method(HttpRequest.Method.GET)
          .build()
      val ins = req.execute().inputStream()
      var bmp: Bitmap
      ins.use { i ->
        bmp = BitmapFactory.decodeStream(i)
      }

      Log.i(TAG, " - icon downloaded for '${podcast.title}': ${bmp.width}x${bmp.height}")

      // TODO: Maybe don't load the whole bitmap just to resize it, we can decode the bounds
      // then use sampling to downscale as wel load it. I think that would be more efficient?
      if (bmp.width > MAX_SIZE || bmp.height > MAX_SIZE) {
        var destWidth = MAX_SIZE
        var destHeight = MAX_SIZE
        if (bmp.width > bmp.height) {
          destHeight = (bmp.height / bmp.width) * MAX_SIZE
        } else if (bmp.height < bmp.width) {
          destWidth = (bmp.width / bmp.height) * MAX_SIZE
        }
        Log.i(TAG, " - resizing '${podcast.title}' icon to: ${destWidth}x$destHeight")
        val oldBmp = bmp
        bmp = Bitmap.createScaledBitmap(bmp, destWidth, destHeight, true)
        oldBmp.recycle()
      }

      val file = cacheFile(podcast)
      Log.i(TAG, " - saving '${podcast.title}' icon to: ${file.absolutePath}")
      bmp.compress(Bitmap.CompressFormat.PNG, 100, file.outputStream())
    }
  }

  private fun cacheFile(podcast: Podcast): File {
    val file = File(appContext.cacheDir, "podcasts/${podcast.id}/icon.png")
    file.parentFile?.mkdirs()
    return file
  }
}