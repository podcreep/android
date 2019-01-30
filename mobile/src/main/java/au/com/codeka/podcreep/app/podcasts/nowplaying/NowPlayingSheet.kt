package au.com.codeka.podcreep.app.podcasts.nowplaying

import android.content.Context
import android.graphics.Color
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.transition.Scene
import android.transition.TransitionManager
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.transition.Fade
import android.transition.TransitionSet
import au.com.codeka.podcreep.app.service.MediaServiceClient
import au.com.codeka.podcreep.databinding.NowPlayingSheetBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import au.com.codeka.podcreep.databinding.NowPlayingSheetExpandedBinding


class NowPlayingSheet(context: Context, attributeSet: AttributeSet)
  : FrameLayout(context, attributeSet), NowPlayingCallbacks {

  private var currPlaybackState: PlaybackStateCompat? = null
  private var currMetadata: MediaMetadataCompat? = null

  private val transitions: TransitionSet
  private val collapsedBinding: NowPlayingSheetBinding
  private val collapsedScene: Scene
  private val expandedBinding: NowPlayingSheetExpandedBinding
  private val expandedScene: Scene
  private var lastState: Int = BottomSheetBehavior.STATE_COLLAPSED

  init {
    val inflater = LayoutInflater.from(context)
    collapsedBinding = NowPlayingSheetBinding.inflate(inflater, this, true)
    collapsedBinding.callbacks = this
    collapsedBinding.executePendingBindings()
    collapsedScene = Scene(this, collapsedBinding.root)

    expandedBinding = NowPlayingSheetExpandedBinding.inflate(inflater, this, false)
    expandedBinding.callbacks = this
    expandedBinding.executePendingBindings()
    expandedScene = Scene(this, expandedBinding.root)

    val shared = TransitionSet()
    shared.addTarget("logo")
    val fade = Fade()
    fade.excludeTarget("logo", true)
    transitions = TransitionSet()
    transitions.addTransition(shared).addTransition(fade)

    setBackgroundColor(Color.WHITE)
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()

    MediaServiceClient.i.addCallback(mediaCallback)

    val behavior = BottomSheetBehavior.from(this)
    behavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
      override fun onStateChanged(bottomSheet: View, newState: Int) {
        if (lastState == BottomSheetBehavior.STATE_SETTLING
            || lastState == BottomSheetBehavior.STATE_DRAGGING) {
          if (newState == BottomSheetBehavior.STATE_EXPANDED) {
            TransitionManager.go(expandedScene, transitions)
          } else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
            TransitionManager.go(collapsedScene, transitions)
          }
        }
        lastState = newState
      }

      override fun onSlide(bottomSheet: View, slideOffset: Float) {
        // Called when the bottom sheet is being dragged
      }
    })
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()

    MediaServiceClient.i.removeCallback(mediaCallback)
  }

  override fun onPlayPauseClick() {
    if (currPlaybackState?.state == PlaybackStateCompat.STATE_PLAYING) {
      MediaServiceClient.i.pause()
    } else {
      MediaServiceClient.i.play()
    }
  }

  private val mediaCallback = object : MediaControllerCompat.Callback() {
    override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
      currMetadata = metadata

      if (metadata != null) {
        collapsedBinding.metadata = metadata
        collapsedBinding.albumArtUri =
            metadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI)
        collapsedBinding.executePendingBindings()

        expandedBinding.metadata = metadata
        expandedBinding.albumArtUri =
            metadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI)
        expandedBinding.executePendingBindings()
      }
    }

    override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
      currPlaybackState = state
    }
  }
}
