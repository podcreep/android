package au.com.codeka.podcreep.app.service

import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat.MediaItem
import android.support.v4.media.MediaBrowserServiceCompat
import android.support.v4.media.session.MediaSessionCompat

import java.util.ArrayList

/**
 * This is the main media service for Pod Creep. It handles playback and also
 */
class MediaService : MediaBrowserServiceCompat() {

  private lateinit var session: MediaSessionCompat

  override fun onCreate() {
    super.onCreate()

    session = MediaSessionCompat(this, "MediaService")
    sessionToken = session.sessionToken
    session.setCallback(MediaSessionCallback())
    session.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)
  }

  override fun onDestroy() {
    session.release()
  }

  override fun onGetRoot(clientPackageName: String,
                         clientUid: Int,
                         rootHints: Bundle?): MediaBrowserServiceCompat.BrowserRoot? {
    return MediaBrowserServiceCompat.BrowserRoot("root", null)
  }

  override fun onLoadChildren(parentId: String, result: Result<MutableList<MediaItem>>) {
    result.sendResult(ArrayList())
  }

  private inner class MediaSessionCallback : MediaSessionCompat.Callback() {
    override fun onPlay() {}

    override fun onSkipToQueueItem(queueId: Long) {}

    override fun onSeekTo(position: Long) {}

    override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {}

    override fun onPause() {}

    override fun onStop() {}

    override fun onSkipToNext() {}

    override fun onSkipToPrevious() {}

    override fun onCustomAction(action: String?, extras: Bundle?) {}

    override fun onPlayFromSearch(query: String?, extras: Bundle?) {}
  }
}
