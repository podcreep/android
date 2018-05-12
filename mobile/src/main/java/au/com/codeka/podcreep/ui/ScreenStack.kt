package au.com.codeka.podcreep.ui

import android.support.v7.app.AppCompatActivity
import android.view.ViewGroup
import au.com.codeka.podcreep.util.Event
import java.util.Stack
import kotlin.reflect.KClass

/**
 * [ScreenStack] is used to manage a stack of [Screen]s. The stack shows it's
 * corresponding view in the [ViewGroup] that the stack is created with.
 */
class ScreenStack(
    activity: AppCompatActivity,
    private val container: ViewGroup) {
  private val screens = Stack<ScreenHolder>()
  private val context = ScreenContext(activity, this, container)

  data class ScreenUpdatedEvent (val prev: Screen?, val current: Screen?)
  val screenUpdated = Event<ScreenUpdatedEvent>()

  fun <S: Screen> register(cls: KClass<S>, creator: (ScreenContext) -> Screen) {
    context.registerScreen(cls, creator)
  }

  inline fun <reified S: Screen> register(noinline creator: (ScreenContext) -> Screen) {
    register(S::class, creator)
  }

  val top: Screen?
    get() = if (screens.empty()) null else screens.peek().screen

  /**
   * Push the given [Screen] onto the stack. The currently visible screen (if any) will
   * become hidden (though not destroyed).
   *
   * @param screen The [Screen] to push.
   */
  fun push(screen: Screen, sharedViews: SharedViews? = null) {
    val prev = top

    if (screen.options.isRootScreen) {
      // If the screen is meant to be a root screen, pop everything first.
      while (!screens.empty()) {
        popInternal()
      }
    } else if (!screens.isEmpty()) {
      // This screen is now going to be shown on top of the current top.
      val top = screens.peek()
      top.screen.onHide()
    }

    screens.push(ScreenHolder(screen, sharedViews))
    screen.onCreate(context, container)
    screen.performShow(sharedViews)

    screenUpdated(ScreenUpdatedEvent(prev, screen))
  }

  /** Internal pop, does not fire screensUpdated. */
  private fun popInternal() {
    var screenHolder: ScreenHolder = screens.pop() ?: return

    screenHolder.screen.onHide()
    screenHolder.screen.onDestroy()

    if (!screens.isEmpty()) {
      screenHolder = screens.peek()
      screenHolder.screen.performShow(screenHolder.sharedViews)
    }
  }

  /**
   * Pop the top-most [Screen] from the stack.
   *
   * @return true if there's another [Screen] displaying, or false if we popped the last
   * [Screen].
   */
  fun pop(): Boolean {
    val prev = screens.peek()?.screen
    popInternal()
    screenUpdated(ScreenUpdatedEvent(prev, top))
    return top != null
  }

  /**
   * Pop all screen from the stack, return to blank "home".
   */
  fun home() {
    val prev = screens.peek()?.screen
    while (!screens.empty()) {
      popInternal()
    }
    screenUpdated(ScreenUpdatedEvent(prev, top))
  }

  /** Contains info we need about a [Screen] while it's on the stack.  */
  private class ScreenHolder(val screen: Screen, val sharedViews: SharedViews?)
}
