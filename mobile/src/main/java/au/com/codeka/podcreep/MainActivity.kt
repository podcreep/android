package au.com.codeka.podcreep

import android.os.Bundle
import android.os.Handler
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.view.MenuItem
import android.widget.FrameLayout
import au.com.codeka.podcreep.R.layout.activity
import au.com.codeka.podcreep.app.podcasts.DiscoverScreen
import au.com.codeka.podcreep.concurrency.TaskRunner
import au.com.codeka.podcreep.concurrency.Threads
import au.com.codeka.podcreep.ui.Screen
import au.com.codeka.podcreep.ui.ScreenStack
import au.com.codeka.podcreep.welcome.LoginScreen
import au.com.codeka.podcreep.welcome.WelcomeScreen
import kotlinx.android.synthetic.main.activity.*

/** The main, in fact one-and-only activity. */
class MainActivity : AppCompatActivity() {
  // Will be non-null between onCreate/onDestroy.
  private var screenStack: ScreenStack? = null

  private var actionBarHeight: Int = 0

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(activity)
    setSupportActionBar(toolbar)
    supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    val actionbarSizeTypedArray = obtainStyledAttributes(intArrayOf(android.R.attr.actionBarSize))
    actionBarHeight = actionbarSizeTypedArray.getDimension(0, 0f).toInt()
    actionbarSizeTypedArray.recycle()

    Threads.UI.setThread(Thread.currentThread(), Handler())
    var taskRunner = TaskRunner()

    val ss = ScreenStack(this, content)
    ss.screenUpdated += { (prev: Screen?, current: Screen?) -> onScreensUpdated(prev, current) }
    ss.register<WelcomeScreen> { WelcomeScreen() }
    ss.register<LoginScreen> { LoginScreen(taskRunner) }
    ss.register<DiscoverScreen> { DiscoverScreen() }
    screenStack = ss
    ss.push(WelcomeScreen())
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
      android.R.id.home -> {
        drawer_layout.openDrawer(Gravity.START)
        true
      }
      else -> super.onOptionsItemSelected(item)
    }
  }

  private fun onScreensUpdated(prev: Screen?, current: Screen?) {
    enableActionBar(current?.options?.enableActionBar ?: false)
  }

  private fun enableActionBar(enabled: Boolean) {
    if (enabled) {
      drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
      supportActionBar?.show()
      (content.layoutParams as FrameLayout.LayoutParams).topMargin = actionBarHeight
    } else {
      drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
      supportActionBar?.hide()
      (content.layoutParams as FrameLayout.LayoutParams).topMargin = 0
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    screenStack = null
  }

  override fun onBackPressed() {
    if (!screenStack!!.pop()) {
      super.onBackPressed()
    }
  }
}
