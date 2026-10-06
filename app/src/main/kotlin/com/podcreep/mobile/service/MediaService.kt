@file:OptIn(UnstableApi::class)

package com.podcreep.mobile.service

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import androidx.annotation.OptIn
import androidx.core.content.IntentCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.CommandButton
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.util.concurrent.SettableFuture
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.podcreep.mobile.R
import com.podcreep.mobile.domain.cache.EpisodeMediaCache
import com.podcreep.mobile.domain.cache.PodcastIconCache
import com.podcreep.mobile.util.L
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.core.net.toUri
import com.podcreep.mobile.data.local.Episode
import com.podcreep.mobile.data.local.Podcast

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
    const val CUSTOM_ACTION_FORWARD = "skip_forward_30"
    const val CUSTOM_ACTION_BACK = "skip_back_10"
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

    val customCommandForward = SessionCommand(CUSTOM_ACTION_FORWARD, Bundle.EMPTY)
    val customCommandBack = SessionCommand(CUSTOM_ACTION_BACK, Bundle.EMPTY)

    val rewindButton = CommandButton.Builder()
      .setDisplayName(getString(R.string.skip_back_10))
      .setIconResId(R.drawable.ic_rewind_10_24dp)
      .setSessionCommand(customCommandBack)
      .build()

    val forwardButton = CommandButton.Builder()
      .setDisplayName(getString(R.string.skip_forward_30))
      .setIconResId(R.drawable.ic_forward_30_24dp)
      .setSessionCommand(customCommandForward)
      .build()

    session = MediaLibrarySession.Builder(this, player, MediaLibrarySessionCallback())
      .setCustomLayout(ImmutableList.of(rewindButton, forwardButton))
      .build()

    val notificationProvider = DefaultMediaNotificationProvider(this)
    notificationProvider.setSmallIcon(R.drawable.ic_notification)
    setMediaNotificationProvider(notificationProvider)
  }

  override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? {
    return session
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    val id = super.onStartCommand(intent, flags, startId)
    L.info("onStart $intent $flags")
    return id
  }

  override fun onDestroy() {
    L.info("onDestroy")
    lifecycle.currentState = Lifecycle.State.DESTROYED
    session?.run {
      mediaManager.release()
      release()
    }
    session = null
    super.onDestroy()
  }

  private inner class MediaLibrarySessionCallback : MediaLibrarySession.Callback {
    override fun onConnect(
      session: MediaSession,
      controller: MediaSession.ControllerInfo
    ): MediaSession.ConnectionResult {
      iconCache.onPackageConnected(controller.packageName)

      val connectionResult = super.onConnect(session, controller)
      val customCommandForward = SessionCommand(CUSTOM_ACTION_FORWARD, Bundle.EMPTY)
      val customCommandBack = SessionCommand(CUSTOM_ACTION_BACK, Bundle.EMPTY)

      val playerCommands = connectionResult.availablePlayerCommands.buildUpon()
        .add(Player.COMMAND_SEEK_TO_PREVIOUS)
        .add(Player.COMMAND_SEEK_TO_NEXT)
        .add(Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)
        .add(Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)
        .add(Player.COMMAND_SEEK_FORWARD)
        .add(Player.COMMAND_SEEK_BACK)
        .build()

      val sessionCommands = connectionResult.availableSessionCommands.buildUpon()
        .add(customCommandForward)
        .add(customCommandBack)
        .build()

      return MediaSession.ConnectionResult.accept(
        sessionCommands,
        playerCommands
      )
    }

    override fun onMediaButtonEvent(
      session: MediaSession,
      controllerInfo: MediaSession.ControllerInfo,
      intent: Intent
    ): Boolean {
      val keyEvent = IntentCompat.getParcelableExtra(intent, Intent.EXTRA_KEY_EVENT, KeyEvent::class.java)
      if (keyEvent != null) {
        when (keyEvent.keyCode) {
          KeyEvent.KEYCODE_MEDIA_NEXT,
          KeyEvent.KEYCODE_MEDIA_FAST_FORWARD,
          KeyEvent.KEYCODE_MEDIA_STEP_FORWARD,
          KeyEvent.KEYCODE_MEDIA_SKIP_FORWARD -> {
            if (keyEvent.action == KeyEvent.ACTION_DOWN) {
              mediaManager.skipForward()
            }
            return true
          }
          KeyEvent.KEYCODE_MEDIA_PREVIOUS,
          KeyEvent.KEYCODE_MEDIA_REWIND,
          KeyEvent.KEYCODE_MEDIA_STEP_BACKWARD,
          KeyEvent.KEYCODE_MEDIA_SKIP_BACKWARD -> {
            if (keyEvent.action == KeyEvent.ACTION_DOWN) {
              mediaManager.skipBack()
            }
            return true
          }
        }
      }
      return super.onMediaButtonEvent(session, controllerInfo, intent)
    }

    override fun onGetLibraryRoot(
      session: MediaLibrarySession,
      browser: MediaSession.ControllerInfo,
      params: LibraryParams?
    ): ListenableFuture<LibraryResult<MediaItem>> {
      L.info("onGetLibraryRoot(${browser.packageName})")

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
      L.info("onGetChildren(${parentId})")

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

    /** This is called when the client sets the media item(s) that it will want to play. */
    override fun onSetMediaItems(
      mediaSession: MediaSession,
      controller: MediaSession.ControllerInfo,
      mediaItems: List<MediaItem>,
      startIndex: Int,
      startPositionMs: Long
    ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
      var podcast: Podcast? = null
      var episode: Episode? = null

      val addedItemsFuture = this.onAddMediaItems(mediaSession, controller, mediaItems.toMutableList())
      val addedItems = if (addedItemsFuture.isDone) addedItemsFuture.get() else mediaItems

      val updatedItems = addedItems.map { item ->
        val mediaId = item.mediaId
        val pair = MediaIdBuilder().parse(mediaId)
        if (pair != null) {
          podcast = pair.first
          episode = pair.second
        }

        item
      }

      val p = podcast
      val e = episode
      if (p != null && e != null) {
        mediaManager.notifyPlay(p, e)
        val offset = (e.position ?: 0) * 1000L

        return Futures.immediateFuture(
          MediaSession.MediaItemsWithStartPosition(
            updatedItems,
            startIndex,
            offset
          )
        )
      }

      return super.onSetMediaItems(
        mediaSession,
        controller,
        mediaItems,
        startIndex,
        startPositionMs
      )
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

          val uri = mediaCache.getUri(podcast, episode) ?: episode.mediaUrl.toUri()
          val artworkUri = item.mediaMetadata.artworkUri ?: iconCache.getRemoteUriOrNull(podcast)

          MediaItem.Builder()
            .setMediaId(mediaId)
            .setUri(uri)
            .setMediaMetadata(
              MediaMetadata.Builder()
                .setTitle(episode.title)
                .setArtist(podcast.title)
                .setArtworkUri(artworkUri)
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
