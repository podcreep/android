package au.com.codeka.podcreep.app.service

import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat.MediaItem
import androidx.media.MediaBrowserServiceCompat
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import android.view.KeyEvent
import au.com.codeka.podcreep.model.Episode
import au.com.codeka.podcreep.model.Podcast
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import java.util.*

/**
 * This is the main media service for Pod Creep. It handles playback and also
 */
class MediaService : MediaBrowserServiceCompat() {

  private lateinit var session: MediaSessionCompat
  private lateinit var mediaManager: MediaManager
  private lateinit var notificationManager: NotificationManager

  override fun onCreate() {
    super.onCreate()

    session = MediaSessionCompat(this, "MediaService")
    sessionToken = session.sessionToken
    session.setCallback(MediaSessionCallback())
    session.setFlags(
        MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)

    mediaManager = MediaManager(this, session)
    notificationManager = NotificationManager(this)
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    val id = super.onStartCommand(intent, flags, startId)

    if (intent != null) {
      Log.i("DEANH", "intent=$intent")

      val podcastStr = intent.extras!!["podcast"] as String
      val episodeStr = intent.extras!!["episode"] as String

      val moshi = Moshi.Builder()
          .add(KotlinJsonAdapterFactory())
          .build()
      val podcast = moshi.adapter<Podcast>(Podcast::class.java).fromJson(podcastStr)!!
      val episode = moshi.adapter<Episode>(Episode::class.java).fromJson(episodeStr)!!

      // Display the notification and place the service in the foreground
      notificationManager.refresh(podcast, episode, session.sessionToken)
      notificationManager.startForeground()

      mediaManager.play(podcast, episode)
    }

    return id
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
    override fun onPlay() {
      Log.i("DEANH", "onPlay")
      mediaManager.play()
    }

    override fun onPause() {
      Log.i("DEANH", "onPause")
      mediaManager.pause()
    }

    override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
      Log.i("DEANH", "onPlayFromMediaId($mediaId)")

      val pair = MediaIdBuilder().parse(mediaId!!)
      val podcast = pair!!.first
      var episode = pair.second

      // Display the notification and place the service in the foreground
      notificationManager.refresh(podcast, episode, session.sessionToken)
      notificationManager.startForeground()

      mediaManager.play(podcast, episode)

    }

    override fun onSkipToQueueItem(queueId: Long) {}

    override fun onSeekTo(position: Long) {

    }

    override fun onMediaButtonEvent(mediaButtonEvent: Intent): Boolean {
      Log.i("DEANH", "onMediaButtonEvent($mediaButtonEvent)")
      return super.onMediaButtonEvent(mediaButtonEvent)
    }

    override fun onStop() {
      Log.i("DEANH", "onStop")
      stopSelf()
    }

    override fun onSkipToNext() {
      Log.i("DEANH", "onSkipToNext")
    }

    override fun onSkipToPrevious() {
      Log.i("DEANH", "onSkipToPrevious")
    }

    override fun onCustomAction(action: String?, extras: Bundle?) {
      Log.i("DEANH", "onCustomAction($action)")
    }

    override fun onPlayFromSearch(query: String?, extras: Bundle?) {
      Log.i("DEANH", "onPlayFromSearch($query)")
    }
  }
}
