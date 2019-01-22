package au.com.codeka.podcreep.app.podcasts.nowplaying

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import com.google.android.material.appbar.AppBarLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import au.com.codeka.podcreep.R
import com.google.android.material.bottomsheet.BottomSheetBehavior

class NowPlayingSheet(context: Context, attributeSet: AttributeSet)
  : RelativeLayout(context, attributeSet) {

  init {
    View.inflate(context, R.layout.now_playing_sheet, this)
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()

//    val params = layoutParams as CoordinatorLayout.LayoutParams
//    params.behavior = BottomSheetBehavior<>()
//    requestLayout()
  }
}
