package au.com.codeka.podcreep.ui

import android.os.Build
import android.support.annotation.CallSuper
import android.transition.Scene
import android.transition.TransitionManager
import android.transition.TransitionSet
import android.view.View
import android.view.ViewGroup

import java.util.Random

/**
 * A [Screen] is similar to a fragment, in that it's a place to keep the business logic
 * of a view.
 *
 *
 * Unlike a Fragment, though, a Screen's lifecycle is very simple. It's created, shown, hidden
 * and destroyed. Once created, it can be shown and hidden multiple times (for example as you
 * navigate the backstack, it might be hidden and then shown again).
 */
abstract class Screen {
  //private static final Log log = new Log("Screen");

  private var container: ViewGroup? = null

  /**
   * The [Scene] representing this screen. We use this to transition between this screen
   * and other screens.
   */
  private var scene: Scene? = null

  /**
   * Called before anything else.
   */
  @CallSuper
  open fun onCreate(context: ScreenContext, container: ViewGroup) {
    this.container = container
  }

  /**
   * Called when the screen is shown. Returns the view we should add to the contain (can be null,
   * however, in which case the contain will be empty).
   */
  open fun onShow(): View? {
    return null
  }

  open fun onHide() {}

  open fun onDestroy() {}

  /**
   * Performs the "show". Calls [.onShow] to get the view, then creates a [Scene] (if
   * needed), and transitions to it.
   */
  fun performShow(sharedViews: SharedViews?) {
    val view = onShow()
    if (view != null) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        if (scene == null) {
          scene = Scene(container, view)
        }
        val mainTransition = TransitionSet()
        val fadeTransition = Transitions.fade().clone()
        mainTransition.addTransition(fadeTransition)

        if (sharedViews != null) {
          val transformTransition = Transitions.transform().clone()
          mainTransition.addTransition(transformTransition)
          for (sharedView in sharedViews.sharedViews) {
            if (sharedView.viewId != 0) {
              fadeTransition.excludeTarget(sharedView.viewId, true)
              transformTransition.addTarget(sharedView.viewId)
            } else {
              val name = "shared-" + java.lang.Long.toString(RANDOM.nextLong())
              if (sharedView.fromViewId != 0 && sharedView.toViewId != 0) {
                container!!.findViewById<View>(sharedView.fromViewId).transitionName = name
                view.findViewById<View>(sharedView.toViewId).transitionName = name
              } else if (sharedView.fromView != null && sharedView.toViewId != 0) {
                sharedView.fromView.transitionName = name
                view.findViewById<View>(sharedView.toViewId).transitionName = name
              } else {
                //log.error("Unexpected SharedView configuration.");
              }
              fadeTransition.excludeTarget(name, true)
              transformTransition.addTarget(name)
            }
          }
        }
        TransitionManager.go(scene, mainTransition)
      } else {
        container!!.removeAllViews()
        container!!.addView(view)
      }
    } else {
      container!!.removeAllViews()
    }
  }

  companion object {
    private val RANDOM = Random()
  }
}
