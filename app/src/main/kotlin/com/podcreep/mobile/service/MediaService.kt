package com.podcreep.mobile.service

import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat.MediaItem
import android.support.v4.media.session.MediaSessionCompat
import android.view.KeyEvent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.lifecycleScope
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import com.podcreep.mobile.domain.cache.PodcastIconCache
import com.podcreep.mobile.util.L
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * This is the main media service for Pod Creep. It handles playback and also lets other bits of the UI know what's
 * going on.
 */
@AndroidEntryPoint
class MediaService : MediaBrowserServiceCompat(), LifecycleOwner {
  @Inject lateinit var syncManager: SyncManager
  @Inject lateinit var browseTreeGenerator: BrowseTreeGenerator
  @Inject lateinit var notificationManager: NotificationManager
  @Inject lateinit var mediaManager: MediaManager
  @Inject lateinit var audioFocusManager: AudioFocusManager
  @Inject lateinit var session: MediaSessionCompat
  @Inject lateinit var iconCache: PodcastIconCache

  companion object {
    private val L = L("MediaService")
  }

  override val lifecycle = LifecycleRegistry(this)

  override fun onCreate() {
    super.onCreate()
    L.info("onCreate")

    // We've just been created, so maybe we need to sync.
    lifecycleScope.launch {
      syncManager.maybeSync()
    }

    sessionToken = session.sessionToken
    session.setCallback(MediaSessionCallback())

    lifecycle.currentState = Lifecycle.State.RESUMED
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    MediaButtonReceiver.handleIntent(session, intent);

    val id = super.onStartCommand(intent, flags, startId)
    L.info("onStart %s %d", intent, flags)
    return id
  }

  override fun onDestroy() {
    super.onDestroy()

    L.info("onDestroy")
    lifecycle.currentState = Lifecycle.State.DESTROYED
    session.release()
  }

  override fun onGetRoot(
      clientPackageName: String,
      clientUid: Int,
      rootHints: Bundle?): BrowserRoot? {
    L.info("onGetRoot(%s, %d, %s)", clientPackageName, clientUid, rootHints)

    iconCache.onPackageConnected(clientPackageName)

    return BrowserRoot("root", null)
  }

  override fun onLoadChildren(parentId: String, result: Result<MutableList<MediaItem>>) {
    L.info("onLoadChildren(%s)", parentId)

    lifecycleScope.launch {
      browseTreeGenerator.onLoadChildren(parentId, result)
    }
  }

  private inner class MediaSessionCallback : MediaSessionCompat.Callback() {
    override fun onPlay() {
      L.info("onPlay")
      if (audioFocusManager.request()) {
        mediaManager.play()
      } else {
        L.info("We didn't get audio focus, not playing.")
      }
      notificationManager.startForeground()
    }

    override fun onPause() {
      L.info("onPause")
      mediaManager.pause()
      audioFocusManager.abandon()
    }

    override fun onPlayFromMediaId(mediaId: String, extras: Bundle?) {
      L.info("onPlayFromMediaId($mediaId)")

      val pair = MediaIdBuilder().parse(mediaId)
      if (pair == null) {
        L.warning("couldn't find media with Id $mediaId")
        return
      }
      val podcast = pair.first
      val episode = pair.second

      // Display the notification and place the service in the foreground
      notificationManager.refresh(podcast, episode, session.sessionToken)
      notificationManager.startForeground()

      if (audioFocusManager.request()) {
        mediaManager.play(podcast, episode)
      } else {
        L.info("We didn't get audio focus, not playing.")
      }
    }

    override fun onSkipToQueueItem(queueId: Long) {
      L.info("onSkipToQueueItem(%d)", queueId)
    }

    override fun onSeekTo(position: Long) {
      L.info("onSeekTo(%d)", position)
    }

    override fun onMediaButtonEvent(mediaButtonEvent: Intent): Boolean {
      L.info("onMediaButtonEvent(%s)", mediaButtonEvent)

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
      L.info("onStop")
      stopSelf()
      notificationManager.stopService()
    }

    override fun onSkipToNext() {
      L.info("onSkipToNext")
      mediaManager.skipForward()
    }

    override fun onSkipToPrevious() {
      L.info("onSkipToPrevious")
      mediaManager.skipBack()
    }

    /**
     * We pretend fast forward is skip forward. We'll get these events e.g. via steering wheel
     * buttons in Android Auto.
     */
    override fun onFastForward() {
      L.info("onFastForward")
      mediaManager.skipForward()
    }

    /**
     * We pretend rewind is skip backward. We'll get these events e.g. via steering wheel buttons
     * in Android Auto.
     */
    override fun onRewind() {
      L.info("onRewind")
      mediaManager.skipBack()
    }

    override fun onCustomAction(action: String?, extras: Bundle?) {
      L.info("onCustomAction($action)")
      mediaManager.customAction(action, extras)
    }

    override fun onPlayFromSearch(query: String?, extras: Bundle?) {
      L.info("onPlayFromSearch($query)")
    }
  }
}
