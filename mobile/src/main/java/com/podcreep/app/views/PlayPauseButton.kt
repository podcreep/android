package com.podcreep.app.views

import android.content.Context
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.AttributeSet
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import com.podcreep.App
import com.podcreep.R

class PlayPauseButton(context: Context, attrs: AttributeSet) : AppCompatImageView(context, attrs) {
  sealed class Mode(val styleableInt: Int, @DrawableRes val drawableRes: Int) {
    object PLAY : Mode(0, R.drawable.pause_to_play_animation)
    object PAUSE : Mode(1, R.drawable.play_to_pause_animation)
  }

  private var currentMode: Mode = Mode.PAUSE
    set(value) {
      field = value
      setImageDrawable(field)
      drawable.startAsAnimatable()
    }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    App.i.mediaServiceClient.addCallback(mediaControllerCallback)
    currentMode = Mode.PLAY
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    App.i.mediaServiceClient.removeCallback(mediaControllerCallback)
  }

  private fun Drawable.startAsAnimatable() = (this as Animatable).start()

  private fun setImageDrawable(mode: Mode) {
    val animatedVector = ContextCompat.getDrawable(context, mode.drawableRes)
    this.setImageDrawable(animatedVector)
  }

  private val mediaControllerCallback = object : MediaControllerCompat.Callback() {
    override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
      super.onPlaybackStateChanged(state)

      var newMode = currentMode
      when (state?.state) {
          PlaybackStateCompat.STATE_BUFFERING -> {
            newMode = Mode.PAUSE
          }
          PlaybackStateCompat.STATE_PLAYING -> {
            newMode = Mode.PAUSE
          }
          PlaybackStateCompat.STATE_PAUSED -> {
            newMode = Mode.PLAY
          }
          else -> {
            // what to do?
          }
      }
      if (newMode != currentMode) {
        currentMode = newMode
      }
    }
  }
}