package com.podcreep.mobile.service

import android.content.ComponentName
import android.content.Context
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.podcreep.mobile.data.local.Episode
import com.podcreep.mobile.data.local.Podcast
import com.podcreep.mobile.ui.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * MediaServiceClient is a helper class that uses the media browser/media session API to talk with
 * the media service. Doing it this way means we keep things nicely in sync with other playback
 * state.
 */
class MediaServiceClient @Inject constructor(@ApplicationContext val context: Context) {
  abstract class Callbacks {
    open fun onPlaybackStateChanged(state: PlaybackStateCompat) {}
    open fun onMetadataChanged(metadata: MediaMetadataCompat) {}
  }

  companion object {
    val TAG = "MediaServiceClient"
  }

  private val mediaBrowser: MediaBrowserCompat
  private val callbacks: ArrayList<Callbacks> = ArrayList()
  private var mediaController: MediaControllerCompat? = null

  private var activity: MainActivity? = null

  private var lastPlaybackState: PlaybackStateCompat? = null
  private var lastMetadata: MediaMetadataCompat? = null


  init {
    mediaBrowser = MediaBrowserCompat(
        context,
        ComponentName(context, MediaService::class.java),
        MediaBrowserConnectionCallbacks(),
        null // optional Bundle
    )
    mediaBrowser.connect()
  }

  fun attachActivity(activity: MainActivity) {
    val oldActivity = this.activity
    if (oldActivity != null) {
      // TODO: error?
      detachActivity(oldActivity)
    }
    this.activity = activity
    MediaControllerCompat.setMediaController(activity, mediaController)
  }

  fun detachActivity(activity: MainActivity) {
    if (this.activity != activity) {
      // TODO: error?
      return
    }
    this.activity = null
  }

  fun addCallback(callback: Callbacks) {
    if (callbacks.contains(callback)) {
      return
    }

    callbacks.add(callback)

    val playbackState = lastPlaybackState
    if (playbackState != null) {
      callback.onPlaybackStateChanged(playbackState)
    }
    val metadata = lastMetadata
    if (metadata != null) {
      callback.onMetadataChanged(metadata)
    }
  }

  fun removeCallback(callback: Callbacks) {
    callbacks.remove(callback)
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

  fun skipForward() {
    // TODO: make these custom actions.
    mediaController?.transportControls?.skipToNext()
  }

  fun skipBack() {
    // TODO: make these custom actions.
    mediaController?.transportControls?.skipToPrevious()
  }

  private var controllerCallback = object : MediaControllerCompat.Callback() {
    override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
      if (metadata == null) {
        return
      }
      lastMetadata = metadata

      callbacks.forEach {
        it.onMetadataChanged(metadata)
      }
    }

    override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
      if (state == null) {
        return
      }
      lastPlaybackState = state

      callbacks.forEach {
        it.onPlaybackStateChanged(state)
      }
    }
  }

  private inner class MediaBrowserConnectionCallbacks : MediaBrowserCompat.ConnectionCallback() {
    override fun onConnected() {
      // Get the token for the MediaSession
      mediaBrowser.sessionToken.also { token ->
        // Create a MediaControllerCompat.
        mediaController = MediaControllerCompat(context, token)

        // Get the current values of things.
        lastPlaybackState = mediaController?.playbackState
        lastMetadata = mediaController?.metadata
        controllerCallback.onPlaybackStateChanged(lastPlaybackState)
        controllerCallback.onMetadataChanged(lastMetadata)

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