package com.podcreep.app.podcasts.nowplaying

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
import android.widget.ImageView
import com.podcreep.R
import com.podcreep.app.service.MediaServiceClient
import com.podcreep.databinding.NowPlayingHeaderCollapsedBinding
import com.podcreep.databinding.NowPlayingHeaderExpandedBinding
import com.podcreep.databinding.NowPlayingSheetBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlin.math.abs

/**
 * The {@link NowPlayingSheet} is displayed at the bottom of the screen, you can drag it up to
 * display more podcast_details.
 */
class NowPlayingSheet(context: Context, attributeSet: AttributeSet)
  : FrameLayout(context, attributeSet) {

  private val vm: NowPlayingViewModel = NowPlayingViewModel(null, null)

  private val transitions: TransitionSet
  private val sheetBinding: NowPlayingSheetBinding
  private val collapsedHeaderBinding: NowPlayingHeaderCollapsedBinding
  private val collapsedHeaderScene: Scene
  private val expandedHeaderBinding: NowPlayingHeaderExpandedBinding
  private val expandedHeaderScene: Scene
  private var lastState: Int = BottomSheetBehavior.STATE_COLLAPSED

  init {
    val inflater = LayoutInflater.from(context)
    sheetBinding = NowPlayingSheetBinding.inflate(inflater, this, true)
    sheetBinding.vm = vm
    sheetBinding.executePendingBindings()

    val headerContainer = sheetBinding.root.findViewById<FrameLayout>(R.id.header)

    collapsedHeaderBinding =
        NowPlayingHeaderCollapsedBinding.inflate(inflater, headerContainer, false)
    collapsedHeaderBinding.vm = vm
    collapsedHeaderBinding.executePendingBindings()
    collapsedHeaderScene = Scene(headerContainer, collapsedHeaderBinding.root)

    expandedHeaderBinding =
        NowPlayingHeaderExpandedBinding.inflate(inflater, headerContainer, false)
    expandedHeaderBinding.vm = vm
    expandedHeaderBinding.executePendingBindings()
    expandedHeaderScene = Scene(headerContainer, expandedHeaderBinding.root)

    val shared = TransitionSet()
    shared.addTarget("logo")
    val fade = Fade()
    fade.excludeTarget("logo", true)
    transitions = TransitionSet()
    transitions.addTransition(shared).addTransition(fade)

    // Start off with the collapsed header, no animation.
    headerContainer.addView(collapsedHeaderBinding.root)

    setBackgroundColor(Color.WHITE)
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()

    MediaServiceClient.i.addCallback(mediaCallback)

    val behavior = BottomSheetBehavior.from(this)
    behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
      override fun onStateChanged(bottomSheet: View, newState: Int) {
        if (lastState == BottomSheetBehavior.STATE_SETTLING
            || lastState == BottomSheetBehavior.STATE_DRAGGING) {
          if (newState == BottomSheetBehavior.STATE_EXPANDED) {
            TransitionManager.go(expandedHeaderScene, transitions)
          } else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
            TransitionManager.go(collapsedHeaderScene, transitions)
          }
        }
        lastState = newState
      }

      override fun onSlide(bottomSheet: View, slideOffset: Float) {
        val opacity = abs(slideOffset)
        findViewById<ImageView>(R.id.large_podcast_logo).alpha = opacity
      }
    })
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()

    MediaServiceClient.i.removeCallback(mediaCallback)
  }

  private val mediaCallback = object : MediaControllerCompat.Callback() {
    override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
      vm.metadata = metadata

      if (metadata != null) {
        sheetBinding.vm = vm
        sheetBinding.executePendingBindings()
        collapsedHeaderBinding.vm = vm
        collapsedHeaderBinding.executePendingBindings()
        expandedHeaderBinding.vm = vm
        expandedHeaderBinding.executePendingBindings()
      }
    }

    override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
      vm.playbackState = state

      sheetBinding.vm = vm
      sheetBinding.executePendingBindings()
      collapsedHeaderBinding.vm = vm
      collapsedHeaderBinding.executePendingBindings()
      expandedHeaderBinding.vm = vm
      expandedHeaderBinding.executePendingBindings()
    }
  }
}
