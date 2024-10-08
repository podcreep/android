package com.podcreep.mobile.domain.cache

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import com.podcreep.mobile.data.local.Podcast
import com.podcreep.mobile.util.L
import com.podcreep.mobile.util.Server
import com.podcreep.mobile.util.await
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A cache of the podcast icons. We'll save them to disk and hand out Uris that we can display
 * in the app and/or Android Auto.
 */
@Singleton
class PodcastIconCache @Inject constructor(
  @ApplicationContext val appContext: Context,
  private val server: Server) {

  companion object {
    const val MAX_SIZE = 256
  }

  private val L: L = L("PodcastIconCache")
  private val connectedPackages = HashSet<String>()

  /**
   * Called when a package connects to our media service. We'll add it to a list of packages we'll
   * allow to access our remote Uris.
   *
   * Note: currently there's no way to remove packages.
   */
  fun onPackageConnected(packageName: String) {
    connectedPackages.add(packageName)
  }

  /** Get a {@link Uri} that can be called from remote processes (such as Android Auto). */
  suspend fun getRemoteUri(podcast: Podcast): Uri {
    var uri = getRemoteUriOrNull(podcast)
    if (uri == null) {
      refresh(podcast)
      uri = getRemoteUriOrNull(podcast)!!
    }

    return uri
  }

  /**
   * Get a {@link Uri} that can be called from remote process (such as Android Auto). But unlike
   * {@link #getRemoteUri} this won't try to refresh the podcast icon if it's not already
   * downloaded.
   */
  fun getRemoteUriOrNull(podcast: Podcast): Uri? {
    val file = cacheFile(podcast)
    if (!file.exists()) {
      return null
    }

    val uri = FileProvider.getUriForFile(appContext, "com.podcreep.fileprovider", file)
    // Grant everybody who has connected to us access to our URI.
    for (packageName in connectedPackages) {
      appContext.grantUriPermission(
        packageName, uri,
        Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    return uri
  }

  /** Get a {@link Uri} that can be called only from this process (e.g. our UI). */
  suspend fun getLocalUri(podcast: Podcast): Uri {
    val file = cacheFile(podcast)
    if (!file.exists()) {
      refresh(podcast)
    }

    return Uri.fromFile(file)
  }

  /** Refresh the cached icon of the given {@link Podcast}. */
  suspend fun refresh(podcast: Podcast) {
    L.info("Downloading icon for '${podcast.title}'")
    var url = podcast.imageUrl
    url += "?width=${MAX_SIZE}&height=${MAX_SIZE}"
    val req = server.request(url).get()
    val resp = server.call(req).await()
    val src = resp.body?.source()
    if (src == null) {
      L.warning("error fetching icon: ${resp.code} ${resp.message}")
      return
    }
    var bmp: Bitmap = BitmapFactory.decodeStream(src.inputStream())
    L.info(" - icon downloaded for '${podcast.title}': ${bmp.width}x${bmp.height}")

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
      L.info(" - resizing '${podcast.title}' icon to: ${destWidth}x$destHeight")
      val oldBmp = bmp
      bmp = Bitmap.createScaledBitmap(bmp, destWidth, destHeight, true)
      oldBmp.recycle()
    }

    val file = cacheFile(podcast)
    L.info(" - saving '${podcast.title}' icon to: ${file.absolutePath}")
    bmp.compress(Bitmap.CompressFormat.PNG, 100, file.outputStream())
  }

  private fun cacheFile(podcast: Podcast): File {
    val file = File(appContext.cacheDir, "podcasts/${podcast.id}/icon.png")
    file.parentFile?.mkdirs()
    return file
  }
}