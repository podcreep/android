package au.com.codeka.podcreep.model.cache

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import au.com.codeka.podcreep.concurrency.TaskRunner
import au.com.codeka.podcreep.concurrency.Threads
import au.com.codeka.podcreep.model.store.Podcast
import au.com.codeka.podcreep.model.store.Store
import au.com.codeka.podcreep.net.HttpRequest
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

  /** Get a {@link Uri} that can be called from remote processes (such as Android Auto). */
  fun getRemoteUri(podcast: Podcast): Uri {
    val file = cacheFile(podcast)
    if (!file.exists()) {
      refresh(podcast)
    }

    val uri = FileProvider.getUriForFile(appContext, "au.com.codeka.podcreep.fileprovider", file)
    // Grant everybody access to our URI. TODO: only the media controller attached to us...
    for (ri in appContext.packageManager.getInstalledPackages(PackageManager.GET_SERVICES)) {
      appContext.grantUriPermission(ri.packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
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
    taskRunner.runTask({
      Log.i(TAG, "Downloading icon for '${podcast.title}'")
      val req = HttpRequest.Builder()
          .url(podcast.imageUrl)
          .method(HttpRequest.Method.GET)
          .build()
      val ins = req.execute().inputStream()
      var bmp: Bitmap
      try {
        bmp = BitmapFactory.decodeStream(ins)
      } finally {
        ins.close()
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
    }, Threads.BACKGROUND)
  }

  private fun cacheFile(podcast: Podcast): File {
    val file = File(appContext.cacheDir, "podcasts/${podcast.id}/icon.png")
    file.parentFile?.mkdirs()
    return file
  }
}