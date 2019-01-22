package au.com.codeka.podcreep.app.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import au.com.codeka.podcreep.MainActivity
import au.com.codeka.podcreep.model.Episode
import au.com.codeka.podcreep.model.Podcast

/**
 * MediaServiceClient is a helper class that uses the media browser/media session API to talk
 * with the media service. Doing it this ways means we keep things nicely in sync with other
 * playback state.
 */
class MediaServiceClient {
  companion object {
    val i: MediaServiceClient = MediaServiceClient()
  }

  private var activity: MainActivity? = null
  private var mediaBrowser: MediaBrowserCompat? = null
  private var mediaController: MediaControllerCompat? = null

  fun setup(activity: MainActivity) {
    this.activity = activity
    mediaBrowser = MediaBrowserCompat(
        activity,
        ComponentName(activity, MediaService::class.java),
        mediaBrowserConnectionCallbacks,
        null // optional Bundle
    )
    mediaBrowser?.connect()
  }

  fun destroy() {
    mediaController?.unregisterCallback(controllerCallback)
    mediaBrowser?.disconnect()

    mediaController = null
    mediaBrowser = null
    activity = null
  }

  fun play(podcast: Podcast, episode: Episode) {
    val mediaIdBuilder = MediaIdBuilder()
    mediaController?.transportControls?.playFromMediaId(
        mediaIdBuilder.getMediaId(podcast, episode), null)
  }

  fun play() {
    mediaController?.transportControls?.play()
  }

  fun pause() {
    mediaController?.transportControls?.pause()
  }

  private var controllerCallback = object : MediaControllerCompat.Callback() {
    override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
      // TODO: notify UI
      Log.i("DEANH", "metadata changed!")
    }

    override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
      // TODO: notify UI
      Log.i("DEANH", "playback state changed!")
      
    }
  }

  private val mediaBrowserConnectionCallbacks = object : MediaBrowserCompat.ConnectionCallback() {
    override fun onConnected() {
      if (activity == null) {
        // Activity was finished before we connected.
        return
      }
      // Get the token for the MediaSession
      mediaBrowser?.sessionToken.also { token ->
        // Create a MediaControllerCompat.
        mediaController = MediaControllerCompat(activity, token!!)
        MediaControllerCompat.setMediaController(activity!!, mediaController)

        // Register a Callback to stay in sync
        mediaController?.registerCallback(controllerCallback)
      }
    }

    override fun onConnectionSuspended() {
      // The Service has crashed. Disable transport controls until it automatically reconnects
    }

    override fun onConnectionFailed() {
      // The Service has refused our connection
    }
  }
}