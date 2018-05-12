package au.com.codeka.podcreep.ui

import android.support.v7.app.AppCompatActivity
import android.view.ViewGroup
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


  fun <S: Screen> register(cls: KClass<S>, creator: (ScreenContext) -> Screen) {
    context.registerScreen(cls, creator)
  }

  inline fun <reified S: Screen> register(noinline creator: (ScreenContext) -> Screen) {
    register(S::class, creator)
  }

  /**
   * Push the given [Screen] onto the stack. The currently visible screen (if any) will
   * become hidden (though not destroyed).
   *
   * @param screen The [Screen] to push.
   */
  fun push(screen: Screen, sharedViews: SharedViews? = null) {
    if (!screens.isEmpty()) {
      val top = screens.peek()
      top.screen.onHide()
    }

//    if (screens.contains(screen)) {
//      // If the screen is already on the stack, we'll just remove everything up to that screen
//      while (screens.peek().screen !== screen) {
//        pop()
//      }
//    } else {
      screens.push(ScreenHolder(screen, sharedViews))
      screen.onCreate(context, container)
//    }

    screen.performShow(sharedViews)
  }

  /**
   * Pop the top-most [Screen] from the stack.
   *
   * @return true if there's another [Screen] displaying, or false if we popped the last
   * [Screen].
   */
  fun pop(): Boolean {
    var screenHolder: ScreenHolder? = screens.pop() ?: return false

    screenHolder!!.screen.onHide()
    screenHolder.screen.onDestroy()

    if (!screens.isEmpty()) {
      screenHolder = screens.peek()
      screenHolder!!.screen.performShow(screenHolder.sharedViews)
      return true
    }

    return false
  }

  /**
   * Pop all screen from the stack, return to blank "home".
   */
  fun home() {
    while (pop()) {
      // Keep going.
    }
  }

  /** Contains info we need about a [Screen] while it's on the stack.  */
  private class ScreenHolder(val screen: Screen, val sharedViews: SharedViews?)
}
