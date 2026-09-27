package com.podcreep.mobile.service

import android.content.ComponentName
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaBrowser
import androidx.media3.session.SessionToken
import com.podcreep.mobile.data.local.Episode
import com.podcreep.mobile.data.local.Podcast
import com.podcreep.mobile.ui.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.ExecutionException
import javax.inject.Inject

/** MediaServiceClient uses Media3 MediaBrowser to communicate with MediaService. */
class MediaServiceClient @Inject constructor(@param:ApplicationContext val context: Context) {
  abstract class Callbacks {
    open fun onPlaybackStateChanged(isPlaying: Boolean, playbackState: Int) {}
    open fun onMetadataChanged(mediaItem: MediaItem?) {}
  }

  companion object {
    val TAG = "MediaServiceClient"
  }

  private var mediaBrowser: MediaBrowser? = null
  private val callbacks: ArrayList<Callbacks> = ArrayList()
  private var activity: MainActivity? = null

  private var lastIsPlaying: Boolean = false
  private var lastPlaybackState: Int = Player.STATE_IDLE
  private var lastMediaItem: MediaItem? = null

  init {
    val sessionToken = SessionToken(context, ComponentName(context, MediaService::class.java))
    val browserFuture = MediaBrowser.Builder(context, sessionToken).buildAsync()
    browserFuture.addListener({
      try {
        val browser = browserFuture.get()
        mediaBrowser = browser
        browser.addListener(object : Player.Listener {
          override fun onIsPlayingChanged(isPlaying: Boolean) {
            lastIsPlaying = isPlaying
            lastPlaybackState = browser.playbackState
            notifyPlaybackStateChanged()
          }

          override fun onPlaybackStateChanged(playbackState: Int) {
            lastPlaybackState = playbackState
            notifyPlaybackStateChanged()
          }

          override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            lastMediaItem = mediaItem
            notifyMetadataChanged()
          }
        })
        lastIsPlaying = browser.isPlaying
        lastPlaybackState = browser.playbackState
        lastMediaItem = browser.currentMediaItem
        notifyPlaybackStateChanged()
        notifyMetadataChanged()
      } catch (e: ExecutionException) {
        e.printStackTrace()
      } catch (e: InterruptedException) {
        e.printStackTrace()
      }
    }, ContextCompat.getMainExecutor(context))
  }

  fun attachActivity(activity: MainActivity) {
    this.activity = activity
  }

  fun detachActivity(activity: MainActivity) {
    if (this.activity == activity) {
      this.activity = null
    }
  }

  fun addCallback(callback: Callbacks): Callbacks {
    if (!callbacks.contains(callback)) {
      callbacks.add(callback)
      callback.onPlaybackStateChanged(lastIsPlaying, lastPlaybackState)
      callback.onMetadataChanged(lastMediaItem)
    }
    return callback
  }

  fun removeCallback(callback: Callbacks) {
    callbacks.remove(callback)
  }

  fun play(podcast: Podcast, episode: Episode) {
    val mediaId = MediaIdBuilder().getMediaId(podcast, episode)
    mediaBrowser?.setMediaItem(
      MediaItem.Builder()
        .setMediaId(mediaId)
        .setUri(episode.mediaUrl)
        .setMediaMetadata(
          MediaMetadata.Builder()
            .setTitle(episode.title)
            .setArtist(podcast.title)
            .build()
        )
        .build()
    )
    mediaBrowser?.prepare()
    mediaBrowser?.play()
  }

  fun play() {
    mediaBrowser?.play()
  }

  fun pause() {
    mediaBrowser?.pause()
  }

  fun skipForward() {
    mediaBrowser?.seekForward()
  }

  fun skipBack() {
    mediaBrowser?.seekBack()
  }

  private fun notifyPlaybackStateChanged() {
    callbacks.forEach { it.onPlaybackStateChanged(lastIsPlaying, lastPlaybackState) }
  }

  private fun notifyMetadataChanged() {
    callbacks.forEach { it.onMetadataChanged(lastMediaItem) }
  }
}
