package au.com.codeka.podcreep

import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import au.com.codeka.podcreep.R.layout.activity_main
import au.com.codeka.podcreep.app.podcasts.PodcastsScreen
import au.com.codeka.podcreep.concurrency.TaskRunner
import au.com.codeka.podcreep.concurrency.Threads
import au.com.codeka.podcreep.ui.ScreenStack
import au.com.codeka.podcreep.welcome.LoginScreen
import au.com.codeka.podcreep.welcome.WelcomeScreen
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

  // Will be non-null between onCreate/onDestroy.
  private var screenStack: ScreenStack? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(activity_main)

    Threads.UI.setThread(Thread.currentThread(), Handler())
    var taskRunner = TaskRunner()

    val ss = ScreenStack(this, container)
    ss.register<WelcomeScreen> { WelcomeScreen() }
    ss.register<LoginScreen> { LoginScreen(taskRunner) }
    ss.register<PodcastsScreen> { PodcastsScreen() }
    screenStack = ss
    ss.push(WelcomeScreen())
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
