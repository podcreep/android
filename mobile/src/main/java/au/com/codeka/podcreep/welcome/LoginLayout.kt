package au.com.codeka.podcreep.welcome

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import android.widget.TextView
import au.com.codeka.podcreep.R
import kotlinx.android.synthetic.main.login.view.*

/**
 * A login screen that offers login via email/password.
 */
class LoginLayout(context: Context, var callbacks: Callbacks) : LinearLayout(context) {
  interface Callbacks {
    fun onSignIn(username: String, password: String)
  }

  init {
    View.inflate(context, R.layout.login, this)
    password.setOnEditorActionListener(TextView.OnEditorActionListener { _, id, _ ->
      if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
        attemptLogin()
        return@OnEditorActionListener true
      }
      false
    })
    sign_in_btn.setOnClickListener { attemptLogin() }
  }

  /** Show an error message, presumably from a failed login attempt. */
  fun showError(msg: String) {
    showProgress(false)
    username.error = null
    password.error = msg
  }

  /**
   * Attempts to sign in or register the account specified by the login form. Does a quick check for
   * empty username/password before attempting.
   */
  private fun attemptLogin() {
    // Reset errors.
    username.error = null
    password.error = null

    // Store values at the time of the login attempt.
    val usernameStr = username.text.toString()
    val passwordStr = password.text.toString()

    var cancel = false
    var focusView: View? = null

    if (TextUtils.isEmpty(passwordStr)) {
      password.error = context.getString(R.string.error_field_required)
      focusView = password
      cancel = true
    }
    if (TextUtils.isEmpty(usernameStr)) {
      username.error = context.getString(R.string.error_field_required)
      focusView = username
      cancel = true
    }

    if (cancel) {
      focusView?.requestFocus()
    } else {
      showProgress(true)
      callbacks.onSignIn(usernameStr, passwordStr)
    }
  }

  /** Shows the progress UI and hides the login form (and vice-versa). */
  private fun showProgress(show: Boolean) {
    val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()

    login_form.visibility = if (show) View.GONE else View.VISIBLE
    login_form.animate()
        .setDuration(shortAnimTime)
        .alpha((if (show) 0 else 1).toFloat())
        .setListener(object : AnimatorListenerAdapter() {
          override fun onAnimationEnd(animation: Animator) {
            login_form.visibility = if (show) View.GONE else View.VISIBLE
          }
        })

    login_progress.visibility = if (show) View.VISIBLE else View.GONE
    login_progress.animate()
        .setDuration(shortAnimTime)
        .alpha((if (show) 1 else 0).toFloat())
        .setListener(object : AnimatorListenerAdapter() {
          override fun onAnimationEnd(animation: Animator) {
            login_progress.visibility = if (show) View.VISIBLE else View.GONE
          }
        })
  }
}
