package com.podcreep.ui

import android.annotation.TargetApi
import android.os.Build
import android.transition.ChangeBounds
import android.transition.ChangeImageTransform
import android.transition.ChangeTransform
import android.transition.Fade
import android.transition.TransitionSet

/**
 * Helper class for creating transitions between fragments.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
object Transitions {
  class Transform : TransitionSet() {
    init {
      init()
    }

    private fun init() {
      ordering = TransitionSet.ORDERING_TOGETHER
      this.addTransition(ChangeBounds())
          .addTransition(ChangeTransform())
          .addTransition(ChangeImageTransform())
    }
  }

  fun transform(): Transform {
    return Transform()
  }

  fun fade(): Fade {
    return Fade()
  }
}
