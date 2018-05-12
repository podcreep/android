package au.com.codeka.podcreep.ui

import android.view.View

import java.util.ArrayList

/**
 * Container class for holding shared views that we want to animate specially between screen
 * transitions.
 */
class SharedViews private constructor(val sharedViews: ArrayList<SharedView>) {

  class SharedView {
    val viewId: Int
    val fromView: View?
    val fromViewId: Int
    val toViewId: Int

    constructor(viewId: Int) {
      this.viewId = viewId
      this.fromView = null
      this.fromViewId = 0
      this.toViewId = 0
    }

    constructor(fromViewId: Int, toViewId: Int) {
      this.viewId = 0
      this.fromView = null
      this.fromViewId = fromViewId
      this.toViewId = toViewId
    }

    constructor(fromView: View, toViewId: Int) {
      this.viewId = 0
      this.fromView = fromView
      this.fromViewId = 0
      this.toViewId = toViewId
    }
  }

  class Builder {
    private val sharedViews = ArrayList<SharedView>()

    fun addSharedView(viewId: Int): Builder {
      sharedViews.add(SharedView(viewId))
      return this
    }

    fun addSharedView(fromViewId: Int, toViewId: Int): Builder {
      sharedViews.add(SharedView(fromViewId, toViewId))
      return this
    }

    fun addSharedView(fromView: View, toViewId: Int): Builder {
      sharedViews.add(SharedView(fromView, toViewId))
      return this
    }

    fun build(): SharedViews {
      return SharedViews(sharedViews)
    }
  }

  companion object {

    fun builder(): Builder {
      return Builder()
    }
  }
}
