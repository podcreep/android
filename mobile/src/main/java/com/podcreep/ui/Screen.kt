package com.podcreep.ui

import android.transition.Scene
import android.transition.TransitionManager
import android.transition.TransitionSet
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import java.util.*

/**
 * A [Screen] is similar to a fragment, in that it's a place to keep the business logic
 * of a view.
 *
 *
 * Unlike a Fragment, though, a Screen's lifecycle is very simple. It's created, shown, hidden
 * and destroyed. Once created, it can be shown and hidden multiple times (for example as you
 * navigate the backstack, it might be hidden and then shown again).
 */
abstract class Screen : LifecycleOwner {
  //private static final Log log = new Log("Screen");

  private lateinit var lifecycleRegistry: LifecycleRegistry
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

    lifecycleRegistry = LifecycleRegistry(this)
    lifecycleRegistry.currentState = Lifecycle.State.CREATED
  }

  /**
   * Called when the screen is shown. Returns the view we should add to the contain (can be null,
   * however, in which case the contain will be empty).
   */
  @CallSuper
  open fun onShow(): View? {
    lifecycleRegistry.currentState = Lifecycle.State.RESUMED
    return null
  }

  /**
   * Called when the user selects a menu item on the app bar. Return true if you handled the item or
   * false if not.
   */
  open fun onMenuItemSelected(item: MenuItem): Boolean {
    return false
  }

  open fun onHide() {}

  @CallSuper
  open fun onDestroy() {
    lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
  }

  /**
   * Get the [ScreenOptions] for this screen. By default, just returns the default options.
   */
  open val options: ScreenOptions
    get() = ScreenOptions()

  /**
   * Gets a {@link Lifecycle} for this screen.
   */
  override fun getLifecycle(): Lifecycle {
    return lifecycleRegistry
  }

  /**
   * Performs the "show". Calls [.onShow] to get the view, then creates a [Scene] (if
   * needed), and transitions to it.
   */
  fun performShow(sharedViews: SharedViews?) {
    val view = onShow()
    if (view != null) {
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
    }
  }

  companion object {
    private val RANDOM = Random()
  }
}
