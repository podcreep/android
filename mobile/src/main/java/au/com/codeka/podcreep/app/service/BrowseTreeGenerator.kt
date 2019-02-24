package au.com.codeka.podcreep.app.service

import android.support.v4.media.MediaBrowserCompat
import androidx.media.MediaBrowserServiceCompat
import java.util.ArrayList

class BrowseTreeGenerator {
  fun onLoadChildren(parentId: String, result: MediaBrowserServiceCompat.Result<MutableList<MediaBrowserCompat.MediaItem>>) {
    result.sendResult(ArrayList())

  }
}