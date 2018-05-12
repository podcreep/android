package au.com.codeka.podcreep.ui

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.view.ViewGroup
import kotlin.reflect.KClass

/**
 * A context object that's passed to [Screen]s to allow them to access some Android
 * functionality (such as starting activities) or screen functionality (such as pushing new
 * screens, popping the backstack, etc).
 */
class ScreenContext(
    val activity: AppCompatActivity,
    private val stack: ScreenStack,
    private val container: ViewGroup) {
  private val creators = HashMap<KClass<out Screen>, (ScreenContext) -> Screen>()

  fun <S: Screen> registerScreen(cls: KClass<S>, creator: (ScreenContext) -> Screen) {
    creators[cls] = creator
  }

  fun startActivity(intent: Intent) {
    container.context.startActivity(intent)
  }

  inline fun <reified S: Screen> pushScreen() {
    pushScreen(S::class, null)
  }

  inline fun <reified S: Screen> pushScreen(sharedViews: SharedViews) {
    pushScreen(S::class, sharedViews)
  }

  fun <S: Screen> pushScreen(cls: KClass<S>, sharedViews: SharedViews?) {
    val creator = creators[cls] ?: throw IllegalArgumentException("cls does not have a registered ScreenCreator.")
    stack.push(creator.invoke(this), sharedViews)
  }

  fun popScreen() {
    stack.pop()
  }
}
