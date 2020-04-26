package au.com.codeka.podcreep

import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.GravityCompat
import au.com.codeka.podcreep.R.layout.activity
import au.com.codeka.podcreep.app.podcasts.podcast.PodcastDetailsScreen
import au.com.codeka.podcreep.app.podcasts.discover.DiscoverScreen
import au.com.codeka.podcreep.app.service.MediaServiceClient
import au.com.codeka.podcreep.concurrency.Threads
import au.com.codeka.podcreep.ui.Screen
import au.com.codeka.podcreep.ui.ScreenContext
import au.com.codeka.podcreep.ui.ScreenStack
import au.com.codeka.podcreep.app.welcome.LoginScreen
import au.com.codeka.podcreep.app.welcome.WelcomeScreen
import kotlinx.android.synthetic.main.activity.*
import android.util.TypedValue
import android.view.Menu
import androidx.lifecycle.LiveData
import au.com.codeka.podcreep.app.podcasts.episode.EpisodeDetailsScreen
import au.com.codeka.podcreep.app.podcasts.subscriptions.SubscriptionsScreen
import au.com.codeka.podcreep.app.service.SyncManager
import au.com.codeka.podcreep.model.store.Podcast
import au.com.codeka.podcreep.net.HttpException
import au.com.codeka.podcreep.net.HttpRequest
import kotlinx.android.synthetic.main.drawer_header.view.*


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

    // Make sure the sync worker is set up to periodically sync with the server. Also, do a sync now
    // if we haven't done one in a while.
    App.i.syncManager.maybeEnqueue()
    App.i.syncManager.maybeSync()

    val actionbarSizeTypedArray = obtainStyledAttributes(intArrayOf(android.R.attr.actionBarSize))
    actionBarHeight = actionbarSizeTypedArray.getDimension(0, 0f).toInt()
    actionbarSizeTypedArray.recycle()

    MediaServiceClient.i.setup(this)

    val ss = ScreenStack(this, content)
    ss.screenUpdated += { (prev: Screen?, current: Screen?) -> onScreensUpdated(prev, current) }
    ss.register<WelcomeScreen> { _: ScreenContext, _: Array<Any>? -> WelcomeScreen() }
    ss.register<LoginScreen> { _: ScreenContext, _: Array<Any>? -> LoginScreen(App.i.taskRunner) }
    ss.register<DiscoverScreen> {
      _: ScreenContext, _: Array<Any>? -> DiscoverScreen(App.i.taskRunner, App.i.store)
    }
    ss.register<PodcastDetailsScreen> {
      _: ScreenContext,
      params: Array<Any>? -> {
        @Suppress("UNCHECKED_CAST")
        val podcast = params?.get(0) as LiveData<Podcast>
      PodcastDetailsScreen(App.i.taskRunner, App.i.store, podcast.value!!.id, podcast)
      }()
    }
    ss.register<EpisodeDetailsScreen> {
      _: ScreenContext,
      params: Array<Any>? -> {
        @Suppress("UNCHECKED_CAST")
        val data = params?.get(0) as EpisodeDetailsScreen.Data
        EpisodeDetailsScreen(
            App.i.taskRunner, App.i.store, App.i.mediaCache, data.podcast, data.episode)
      }()
    }
    screenStack = ss

    val s = Settings(this)
    if (s.getString(Settings.COOKIE) != "") {
      // TODO: go back to the screen you were on.
      ss.push(SubscriptionsScreen(App.i.store))
    } else {
      ss.push(WelcomeScreen())
    }

    HttpRequest.addGlobalErrorHandler(object : HttpRequest.ErrorHandler{
      override fun onError(e: HttpException) {
        if (e.statusCode == 401) {
          App.i.taskRunner.runTask({
            ss.home()
            ss.push(WelcomeScreen())
          }, Threads.UI)
        }
      }
    })

    nav_view.setNavigationItemSelectedListener {
      when (it.itemId) {
        R.id.nav_subscriptions -> {
          ss.home()
          ss.push(SubscriptionsScreen(App.i.store))
          true
        }
        R.id.nav_discover -> {
          ss.home()
          ss.push(DiscoverScreen(App.i.taskRunner, App.i.store))
          true
        }
        R.id.nav_refresh -> {
          App.i.syncManager.sync()
          true
        }
        else -> false
      }
    }
    nav_view.getHeaderView(0).app_version.text = String.format("v%s", BuildConfig.VERSION_NAME)

    MediaServiceClient.i.addCallback(mediaCallback)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
      android.R.id.home -> {
        if (screenStack?.depth ?: 0 > 1) {
          // If you're deeper in the screen stack, the home button is "back".
          screenStack?.pop()
        } else {
          // TODO: animate some kind of transition or something?
          drawer_layout.openDrawer(GravityCompat.START)
        }
        true
      }
      else -> {
        val handled = screenStack?.top?.onMenuItemSelected(item)
        if (handled != null && handled) {
          return true
        }

        super.onOptionsItemSelected(item)
      }
    }
  }

  override fun onCreateOptionsMenu(menu: Menu?): Boolean {
    val menuId = screenStack?.top?.options?.actionBarMenu
    if (menuId == null) {
      menu?.clear()
    } else {
      menuInflater.inflate(menuId, menu)
    }

    return true
  }

  private fun onScreensUpdated(prev: Screen?, current: Screen?) {
    enableActionBar(current?.options?.enableActionBar ?: false)
    invalidateOptionsMenu()

    // Change the home button to a back button if we're deep in the hierarchy.
    // TODO: animate the transition
    if (screenStack!!.depth > 1) {
      supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp)
    } else {
      supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp)
    }
  }

  private fun enableActionBar(enabled: Boolean) {
    if (enabled) {
      drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
      supportActionBar?.show()
      (content.layoutParams as CoordinatorLayout.LayoutParams).topMargin = actionBarHeight
    } else {
      drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
      supportActionBar?.hide()
      (content.layoutParams as CoordinatorLayout.LayoutParams).topMargin = 0
    }
  }

  override fun onDestroy() {
    super.onDestroy()

    MediaServiceClient.i.removeCallback(mediaCallback)
    MediaServiceClient.i.destroy()

    screenStack = null
  }

  override fun onBackPressed() {
    if (!screenStack!!.pop()) {
      super.onBackPressed()
    }
  }

  private var mediaCallback = object : MediaControllerCompat.Callback() {
    override fun onMetadataChanged(metadata: MediaMetadataCompat?) {

    }

    override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
      // If we're not stopped, then set the now playing control visible.
      var contentMarginPx = 0
      if (state?.state != PlaybackStateCompat.STATE_STOPPED &&
          state?.state != PlaybackStateCompat.STATE_NONE) {
        now_playing.visibility = View.VISIBLE
        shadow.visibility = View.VISIBLE

        contentMarginPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            64.0f,
            resources.displayMetrics).toInt()
      } else {
        now_playing.visibility = View.GONE
        shadow.visibility = View.GONE
      }

      (content.layoutParams as CoordinatorLayout.LayoutParams).bottomMargin = contentMarginPx
    }
  }

}
