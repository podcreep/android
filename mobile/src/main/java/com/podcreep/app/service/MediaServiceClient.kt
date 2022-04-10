package com.podcreep.app.service

import android.content.ComponentName
import android.content.Context
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.podcreep.MainActivity
import com.podcreep.model.store.Episode
import com.podcreep.model.store.Podcast

/**
 * MediaServiceClient is a helper class that uses the media browser/media session API to talk with the media service.
 * Doing it this way means we keep things nicely in sync with other playback state.
 */
class MediaServiceClient(private val context: Context) {
  companion object {
    val TAG = "MediaServiceClient"
  }

  private val mediaBrowser: MediaBrowserCompat
  private val callbacks: ArrayList<MediaControllerCompat.Callback> = ArrayList()
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

  fun addCallback(callback: MediaControllerCompat.Callback) {
    callbacks.add(callback)

    if (lastPlaybackState != null) {
      callback.onPlaybackStateChanged(lastPlaybackState!!)
    }
    if (lastMetadata != null) {
      callback.onMetadataChanged(lastMetadata!!)
    }
  }

  fun removeCallback(callback: MediaControllerCompat.Callback) {
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
      callbacks.forEach {
        it.onMetadataChanged(metadata)
      }
    }

    override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
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