package au.com.codeka.podcreep.app.service

import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat.MediaItem
import androidx.media.MediaBrowserServiceCompat
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import au.com.codeka.podcreep.App
import au.com.codeka.podcreep.model.Episode
import au.com.codeka.podcreep.model.Podcast
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi

/**
 * This is the main media service for Pod Creep. It handles playback and also
 */
class MediaService : MediaBrowserServiceCompat() {
  companion object {
    private const val TAG = "MediaService"
  }

  private lateinit var session: MediaSessionCompat
  private lateinit var mediaManager: MediaManager
  private lateinit var notificationManager: NotificationManager
  private lateinit var browseTreeGenerator: BrowseTreeGenerator
  private lateinit var audioFocusManager: AudioFocusManager

  override fun onCreate() {
    super.onCreate()

    session = MediaSessionCompat(this, "MediaService")
    sessionToken = session.sessionToken
    session.setCallback(MediaSessionCallback())
    session.setFlags(
        MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)

    mediaManager = MediaManager(this, session, App.i.taskRunner)
    notificationManager = NotificationManager(this)
    audioFocusManager = AudioFocusManager(this, mediaManager)

    browseTreeGenerator = BrowseTreeGenerator(App.i.taskRunner)
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    val id = super.onStartCommand(intent, flags, startId)

    if (intent != null) {
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

  override fun onGetRoot(
      clientPackageName: String,
      clientUid: Int,
      rootHints: Bundle?): MediaBrowserServiceCompat.BrowserRoot? {
    return MediaBrowserServiceCompat.BrowserRoot("root", null)
  }

  override fun onLoadChildren(parentId: String, result: Result<MutableList<MediaItem>>) {
    browseTreeGenerator.onLoadChildren(parentId, result)
  }

  private inner class MediaSessionCallback : MediaSessionCompat.Callback() {
    override fun onPlay() {
      Log.i(TAG, "onPlay")
      if (audioFocusManager.request()) {
        mediaManager.play()
      } else {
        Log.i(TAG, "We didn't get audio focus, not playing.")
      }
    }

    override fun onPause() {
      Log.i(TAG, "onPause")
      mediaManager.pause()
      audioFocusManager.abandon()
    }

    override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
      Log.i(TAG, "onPlayFromMediaId($mediaId)")

      val pair = MediaIdBuilder().parse(mediaId!!)
      val podcast = pair!!.first
      val episode = pair.second

      // Display the notification and place the service in the foreground
      notificationManager.refresh(podcast, episode, session.sessionToken)
      notificationManager.startForeground()

      if (audioFocusManager.request()) {
        mediaManager.play(podcast, episode)
      } else {
        Log.i(TAG, "We didn't get audio focus, not playing.")
      }
    }

    override fun onSkipToQueueItem(queueId: Long) {}

    override fun onSeekTo(position: Long) {

    }

    override fun onMediaButtonEvent(mediaButtonEvent: Intent): Boolean {
      Log.i(TAG, "onMediaButtonEvent($mediaButtonEvent)")
      return super.onMediaButtonEvent(mediaButtonEvent)
    }

    override fun onStop() {
      Log.i(TAG, "onStop")
      stopSelf()
    }

    override fun onSkipToNext() {
      mediaManager.skipForward()
    }

    override fun onSkipToPrevious() {
      mediaManager.skipBack()
    }

    override fun onCustomAction(action: String?, extras: Bundle?) {
      Log.i(TAG, "onCustomAction($action)")
    }

    override fun onPlayFromSearch(query: String?, extras: Bundle?) {
      Log.i(TAG, "onPlayFromSearch($query)")
    }
  }
}
