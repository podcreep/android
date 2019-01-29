package au.com.codeka.podcreep.app.views

import android.content.Context
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import au.com.codeka.podcreep.R

class PlayPauseButton(context: Context, attrs: AttributeSet) : ImageView(context, attrs) {

  sealed class Mode(val styleableInt: Int, @DrawableRes val drawableRes: Int) {
    object PLAY : Mode(0, R.drawable.pause_to_play_animation)
    object PAUSE : Mode(1, R.drawable.play_to_pause_animation)
  }

  private var currentMode: Mode = Mode.PLAY
    set(value) {
      field = value
      setImageDrawable(field)
      drawable.startAsAnimatable()
    }

  init {
    setOnClickListener {
        if (currentMode == Mode.PLAY) {
          currentMode = Mode.PAUSE
        } else {
          currentMode = Mode.PLAY
        }
        drawable.startAsAnimatable()
      }
    currentMode = Mode.PLAY
  }

  private fun Drawable.startAsAnimatable() = (this as Animatable).start()

  private fun setImageDrawable(mode: Mode) {
    val animatedVector = ContextCompat.getDrawable(context, mode.drawableRes)
    this.setImageDrawable(animatedVector)
  }

}