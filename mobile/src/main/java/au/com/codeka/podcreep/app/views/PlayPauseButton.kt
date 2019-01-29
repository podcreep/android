package au.com.codeka.podcreep.app.views

import android.content.Context
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import au.com.codeka.podcreep.R

class PlayPauseButton(context: Context, attrs: AttributeSet) : ImageView(context, attrs) {
  sealed class Mode(val styleableInt: Int, @DrawableRes val drawableRes: Int) {
    object PLAY : Mode(0, R.drawable.pause_to_play_animation)
    object PAUSE : Mode(1, R.drawable.play_to_pause_animation)
  }

  private var realOnClickListener: View.OnClickListener? = null

  private var currentMode: Mode = Mode.PLAY
    set(value) {
      field = value
      setImageDrawable(field)
      drawable.startAsAnimatable()
    }

  init {
    super.setOnClickListener {
        if (currentMode == Mode.PLAY) {
          currentMode = Mode.PAUSE
        } else {
          currentMode = Mode.PLAY
        }
        drawable.startAsAnimatable()

        if (realOnClickListener != null) {
          realOnClickListener!!.onClick(this)
        }
      }
    currentMode = Mode.PAUSE
  }

  override fun setOnClickListener(l: OnClickListener?) {
    realOnClickListener = l
  }

  private fun Drawable.startAsAnimatable() = (this as Animatable).start()

  private fun setImageDrawable(mode: Mode) {
    val animatedVector = ContextCompat.getDrawable(context, mode.drawableRes)
    this.setImageDrawable(animatedVector)
  }

}