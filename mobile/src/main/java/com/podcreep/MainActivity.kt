package com.podcreep

import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.LiveData
import com.podcreep.app.podcasts.discover.DiscoverScreen
import com.podcreep.app.podcasts.episode.EpisodeDetailsScreen
import com.podcreep.app.podcasts.podcast.PodcastDetailsScreen
import com.podcreep.app.podcasts.subscriptions.SubscriptionsScreen
import com.podcreep.app.service.MediaServiceClient
import com.podcreep.app.welcome.LoginScreen
import com.podcreep.app.welcome.WelcomeScreen
import com.podcreep.concurrency.Threads
import com.podcreep.databinding.ActivityBinding
import com.podcreep.model.store.Podcast
import com.podcreep.net.HttpException
import com.podcreep.net.HttpRequest
import com.podcreep.ui.Screen
import com.podcreep.ui.ScreenContext
import com.podcreep.ui.ScreenStack

/** The main, in fact one-and-only activity. */
class MainActivity : AppCompatActivity() {
  private lateinit var screenStack: ScreenStack
  private lateinit var binding: ActivityBinding
  private var actionBarHeight: Int = 0

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    binding = DataBindingUtil.setContentView(this, R.layout.activity)
    setSupportActionBar(binding.toolbar)
    supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    // Make sure the sync worker is set up to periodically sync with the server. Also, do a sync now
    // if we haven't done one in a while.
    App.i.syncManager.maybeEnqueue()
    App.i.syncManager.maybeSync()

    val actionbarSizeTypedArray = obtainStyledAttributes(intArrayOf(android.R.attr.actionBarSize))
    actionBarHeight = actionbarSizeTypedArray.getDimension(0, 0f).toInt()
    actionbarSizeTypedArray.recycle()

    val ss = ScreenStack(this, binding.content)
    ss.screenUpdated += { (_: Screen?, current: Screen?) -> onScreensUpdated(current) }
    ss.register<WelcomeScreen> { _: ScreenContext, _: Array<Any>? -> WelcomeScreen() }
    ss.register<LoginScreen> { _: ScreenContext, _: Array<Any>? -> LoginScreen(App.i.taskRunner) }
    ss.register<DiscoverScreen> {
      _: ScreenContext, _: Array<Any>? -> DiscoverScreen(App.i.taskRunner, App.i.store)
    }
    ss.register<PodcastDetailsScreen> {
      _: ScreenContext,
      params: Array<Any>? ->
        @Suppress("UNCHECKED_CAST")
        val podcast = params?.get(0) as LiveData<Podcast>
      PodcastDetailsScreen(App.i.taskRunner, App.i.store, podcast.value!!.id, podcast)
    }
    ss.register<EpisodeDetailsScreen> {
      _: ScreenContext,
      params: Array<Any>? ->
      @Suppress("UNCHECKED_CAST")
      val data = params?.get(0) as EpisodeDetailsScreen.Data

      EpisodeDetailsScreen(App.i.taskRunner, App.i.store, App.i.mediaCache, data.podcast.value!!, data.episode)
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
          App.i.taskRunner.runTask(Threads.UI) {
            ss.home()
            ss.push(WelcomeScreen())
          }
        }
      }
    })

    binding.navView.setNavigationItemSelectedListener {
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
    val header: TextView = binding.navView.getHeaderView(0).findViewById(R.id.app_version)
    header.text = String.format("v%s", BuildConfig.VERSION_NAME)

    App.i.mediaServiceClient.attachActivity(this)
    App.i.mediaServiceClient.addCallback(mediaCallback)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
      android.R.id.home -> {
        if (screenStack.depth > 1) {
          // If you're deeper in the screen stack, the home button is "back".
          screenStack.pop()
        } else {
          // TODO: animate some kind of transition or something?

          binding.drawerLayout.openDrawer(GravityCompat.START)
        }
        true
      }
      else -> {
        val handled = screenStack.top?.onMenuItemSelected(item)
        if (handled != null && handled) {
          return true
        }

        super.onOptionsItemSelected(item)
      }
    }
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    val menuId = screenStack.top?.options?.actionBarMenu
    if (menuId == null) {
      menu.clear()
    } else {
      menuInflater.inflate(menuId, menu)
    }

    return true
  }

  private fun onScreensUpdated(current: Screen?) {
    enableActionBar(current?.options?.enableActionBar ?: false)
    invalidateOptionsMenu()

    // Change the home button to a back button if we're deep in the hierarchy.
    // TODO: animate the transition
    if (screenStack.depth > 1) {
      supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp)
    } else {
      supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp)
    }
  }

  private fun enableActionBar(enabled: Boolean) {
    if (enabled) {
      binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
      supportActionBar?.show()
      (binding.content.layoutParams as CoordinatorLayout.LayoutParams).topMargin = actionBarHeight
    } else {
      binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
      supportActionBar?.hide()
      (binding.content.layoutParams as CoordinatorLayout.LayoutParams).topMargin = 0
    }
  }

  override fun onDestroy() {
    super.onDestroy()

    App.i.mediaServiceClient.removeCallback(mediaCallback)
    App.i.mediaServiceClient.detachActivity(this)
  }

  override fun onBackPressed() {
    if (!screenStack.pop()) {
      super.onBackPressed()
    }
  }

  private var mediaCallback = object : MediaServiceClient.Callbacks() {
    override fun onMetadataChanged(metadata: MediaMetadataCompat) {

    }

    override fun onPlaybackStateChanged(state: PlaybackStateCompat) {
      // If we're not stopped, then set the now playing control visible.
      var contentMarginPx = 0
      if (state.state != PlaybackStateCompat.STATE_STOPPED &&
          state.state != PlaybackStateCompat.STATE_NONE) {
        binding.nowPlaying.visibility = View.VISIBLE
        binding.shadow.visibility = View.VISIBLE

        contentMarginPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            64.0f,
            resources.displayMetrics).toInt()
      } else {
        binding.nowPlaying.visibility = View.GONE
        binding.shadow.visibility = View.GONE
      }

      (binding.content.layoutParams as CoordinatorLayout.LayoutParams).bottomMargin = contentMarginPx
    }
  }
}
