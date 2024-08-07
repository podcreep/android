package com.podcreep.ui

import android.content.Intent
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
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
  private val creators = HashMap<KClass<out Screen>, (ScreenContext, params: Array<Any>?) -> Screen>()

  fun <S: Screen> registerScreen(cls: KClass<S>, creator: (ScreenContext, params: Array<Any>?) -> Screen) {
    creators[cls] = creator
  }

  fun startActivity(intent: Intent) {
    container.context.startActivity(intent)
  }

  inline fun <reified S: Screen> pushScreen() {
    pushScreen(S::class, null, null)
  }

  inline fun <reified S: Screen> pushScreen(sharedViews: SharedViews?, vararg params: Any) {
    pushScreen(S::class, sharedViews, arrayOf(*params))
  }

  fun <S: Screen> pushScreen(cls: KClass<S>, sharedViews: SharedViews?, params: Array<Any>?) {
    val creator = creators[cls]
        ?: throw IllegalArgumentException("cls does not have a registered ScreenCreator.")

    stack.push(creator.invoke(this, params), sharedViews)
  }

  fun popScreen() {
    stack.pop()
  }
}
