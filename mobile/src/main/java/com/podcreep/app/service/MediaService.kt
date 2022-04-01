package com.podcreep.app.service

import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat.MediaItem
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import android.view.KeyEvent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.media.MediaBrowserServiceCompat
import com.podcreep.App
import com.podcreep.model.store.Episode
import com.podcreep.model.store.Podcast
import com.podcreep.model.sync.data.EpisodeJson
import com.podcreep.model.sync.data.PodcastJson
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi

/**
 * This is the main media service for Pod Creep. It handles playback and also lets other bits of the UI know what's
 * going on.
 */
class MediaService : MediaBrowserServiceCompat(), LifecycleOwner {
  companion object {
    private const val TAG = "MediaService"
  }

  private lateinit var session: MediaSessionCompat
  private lateinit var mediaManager: MediaManager
  private lateinit var notificationManager: NotificationManager
  private lateinit var browseTreeGenerator: BrowseTreeGenerator
  private lateinit var audioFocusManager: AudioFocusManager
  private var lifecycle = LifecycleRegistry(this)

  override fun onCreate() {
    super.onCreate()

    session = MediaSessionCompat(this, "MediaService")
    sessionToken = session.sessionToken
    session.setCallback(MediaSessionCallback())
    session.setFlags(
        MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)

    mediaManager = MediaManager(this, session, App.i.taskRunner, App.i.mediaCache, App.i.store)
    notificationManager = NotificationManager(this, 1234 /* notification_id */, "playback", "Playback service")
    audioFocusManager = AudioFocusManager(this, mediaManager)

    browseTreeGenerator = BrowseTreeGenerator(App.i.store, App.i.iconCache, App.i.mediaCache, this)

    lifecycle.currentState = Lifecycle.State.RESUMED
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    val id = super.onStartCommand(intent, flags, startId)

    if (intent != null) {
      val podcastStr = intent.extras!!["podcast"] as String
      val episodeStr = intent.extras!!["episode"] as String

      val moshi = Moshi.Builder()
          .add(KotlinJsonAdapterFactory())
          .build()
      val podcast = moshi.adapter<Podcast>(PodcastJson::class.java).fromJson(podcastStr)!!
      val episode = moshi.adapter<Episode>(EpisodeJson::class.java).fromJson(episodeStr)!!

      // Display the notification and place the service in the foreground
      notificationManager.refresh(podcast, episode, session.sessionToken)
      notificationManager.startForeground()

      mediaManager.play(podcast, episode)
    }

    return id
  }

  override fun onDestroy() {
    lifecycle.currentState = Lifecycle.State.DESTROYED
    session.release()
  }

  override fun onGetRoot(
      clientPackageName: String,
      clientUid: Int,
      rootHints: Bundle?): BrowserRoot? {

    // If someone's getting our root node, we'll do a sync now so we'll be ready when they request
    // our non-root nodes.
    App.i.syncManager.maybeSync()

    return BrowserRoot("root", null)
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
      val keyEvent = mediaButtonEvent.extras?.getParcelable<KeyEvent>(Intent.EXTRA_KEY_EVENT)
      if (keyEvent == null || keyEvent.action != KeyEvent.ACTION_DOWN) {
        // Not an event we are able to handle.
        return false
      }

      when(keyEvent.keyCode) {
        KeyEvent.KEYCODE_MEDIA_PAUSE -> onPause()
        KeyEvent.KEYCODE_MEDIA_PLAY -> onPlay()
        KeyEvent.KEYCODE_MEDIA_SKIP_FORWARD -> onSkipToNext()
        KeyEvent.KEYCODE_MEDIA_NEXT -> onSkipToNext()
        KeyEvent.KEYCODE_MEDIA_PREVIOUS -> onSkipToPrevious()
        KeyEvent.KEYCODE_MEDIA_SKIP_BACKWARD -> onSkipToPrevious()
        KeyEvent.KEYCODE_MEDIA_STOP -> onStop()
        else -> return false
      }
      return true
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

    /**
     * We pretend fast forward is skip forward. We'll get these events e.g. via steering wheel
     * buttons in Android Auto.
     */
    override fun onFastForward() {
      mediaManager.skipForward()
    }

    /**
     * We pretend rewind is skip backward. We'll get these events e.g. via steering wheel buttons
     * in Android Auto.
     */
    override fun onRewind() {
      mediaManager.skipBack()
    }

    override fun onCustomAction(action: String?, extras: Bundle?) {
      Log.i(TAG, "onCustomAction($action)")
      mediaManager.customAction(action, extras)
    }

    override fun onPlayFromSearch(query: String?, extras: Bundle?) {
      Log.i(TAG, "onPlayFromSearch($query)")
    }
  }

  override fun getLifecycle(): Lifecycle {
    return lifecycle
  }
}
