package com.podcreep.mobile.service

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import androidx.work.impl.utils.futures.SettableFuture
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.podcreep.mobile.domain.cache.EpisodeMediaCache
import com.podcreep.mobile.domain.cache.PodcastIconCache
import com.podcreep.mobile.util.L
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * This is the main media service for Pod Creep. It handles playback and also lets other bits of the
 * UI know what's going on.
 */
@AndroidEntryPoint
class MediaService : MediaLibraryService(), LifecycleOwner {
  @Inject lateinit var syncManager: SyncManager
  @Inject lateinit var browseTreeGenerator: BrowseTreeGenerator
  @Inject lateinit var iconCache: PodcastIconCache
  @Inject lateinit var mediaManager: MediaManager
  @Inject lateinit var mediaCache: EpisodeMediaCache

  private var session: MediaLibrarySession? = null

  companion object {
    private val L = L("MediaService")
  }

  override val lifecycle = LifecycleRegistry(this)

  override fun onCreate() {
    super.onCreate()
    L.info("onCreate")

    lifecycle.currentState = Lifecycle.State.RESUMED

    lifecycleScope.launch {
      syncManager.maybeSync()
    }

    val player = mediaManager.player

    session = MediaLibrarySession.Builder(this, player, MediaLibrarySessionCallback())
      .build()
  }

  override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? {
    return session
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    val id = super.onStartCommand(intent, flags, startId)
    L.info("onStart %s %d", intent, flags)
    return id
  }

  override fun onDestroy() {
    L.info("onDestroy")
    lifecycle.currentState = Lifecycle.State.DESTROYED
    session?.run {
      player.release()
      release()
    }
    session = null
    super.onDestroy()
  }

  private inner class MediaLibrarySessionCallback : MediaLibrarySession.Callback {

    override fun onGetLibraryRoot(
      session: MediaLibrarySession,
      browser: MediaSession.ControllerInfo,
      params: LibraryParams?
    ): ListenableFuture<LibraryResult<MediaItem>> {
      L.info("onGetLibraryRoot(%s)", browser.packageName)

      iconCache.onPackageConnected(browser.packageName)

      val rootItem = MediaItem.Builder()
        .setMediaId("root")
        .setMediaMetadata(
          MediaMetadata.Builder()
            .setIsBrowsable(true)
            .setIsPlayable(false)
            .build()
        )
        .build()

      return Futures.immediateFuture(LibraryResult.ofItem(rootItem, params))
    }

    override fun onGetChildren(
      session: MediaLibrarySession,
      browser: MediaSession.ControllerInfo,
      parentId: String,
      page: Int,
      pageSize: Int,
      params: LibraryParams?
    ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
      L.info("onGetChildren(%s)", parentId)

      val future = SettableFuture.create<LibraryResult<ImmutableList<MediaItem>>>()

      lifecycleScope.launch {
        try {
          val mediaItems = browseTreeGenerator.getChildren(parentId)
          future.set(LibraryResult.ofItemList(mediaItems, params))
        } catch (e: Exception) {
          future.set(LibraryResult.ofError(LibraryResult.RESULT_ERROR_UNKNOWN))
        }
      }

      return future
    }

    override fun onAddMediaItems(
      mediaSession: MediaSession,
      controller: MediaSession.ControllerInfo,
      mediaItems: MutableList<MediaItem>
    ): ListenableFuture<MutableList<MediaItem>> {

      val updatedItems = mediaItems.map { item ->
        val mediaId = item.mediaId
        L.info("onAddMediaItems($mediaId)")

        val pair = MediaIdBuilder().parse(mediaId)
        if (pair != null) {
          val podcast = pair.first
          val episode = pair.second

          val uri = mediaCache.getUri(podcast, episode) ?: Uri.parse(episode.mediaUrl)

          MediaItem.Builder()
            .setMediaId(mediaId)
            .setUri(uri)
            .setMediaMetadata(
              MediaMetadata.Builder()
                .setTitle(episode.title)
                .setArtist(podcast.title)
                .setIsBrowsable(false)
                .setIsPlayable(true)
                .build()
            )
            .build()
        } else {
          L.warning("couldn't find media with Id $mediaId")
          item
        }
      }.toMutableList()

      return Futures.immediateFuture(updatedItems)
    }

    override fun onCustomCommand(
      session: MediaSession,
      controller: MediaSession.ControllerInfo,
      customCommand: SessionCommand,
      args: Bundle
    ): ListenableFuture<SessionResult> {
      L.info("onCustomCommand(${customCommand.customAction})")
      mediaManager.customAction(customCommand.customAction, args)
      return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
    }
  }
}
