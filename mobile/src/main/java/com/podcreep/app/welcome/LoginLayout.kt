package com.podcreep.app.welcome

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.podcreep.R
import com.podcreep.databinding.LoginBinding

/**
 * A login screen that offers login via email/password.
 */
class LoginLayout(context: Context, var callbacks: Callbacks) : ConstraintLayout(context) {
  private var binding: LoginBinding

  interface Callbacks {
    fun onSignIn(username: String, password: String)
  }

  init {
    val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    binding = LoginBinding.inflate(layoutInflater, this)

    binding.password.setOnEditorActionListener(TextView.OnEditorActionListener { _, id, _ ->
      if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
        attemptLogin()
        return@OnEditorActionListener true
      }
      false
    })
    binding.signInBtn.setOnClickListener { attemptLogin() }
    binding.signInBtn.requestFocus()
  }

  /** Show an error message, presumably from a failed login attempt. */
  fun showError(msg: String) {
    showProgress(false)
    binding.username.error = null
    binding.password.error = msg
  }

  /**
   * Attempts to sign in or register the account specified by the login form. Does a quick check for
   * empty username/password before attempting.
   */
  private fun attemptLogin() {
    // Reset errors.
    binding.username.error = null
    binding.password.error = null

    // Store values at the time of the login attempt.
    val usernameStr = binding.username.text.toString()
    val passwordStr = binding.password.text.toString()

    var cancel = false
    var focusView: View? = null

    if (TextUtils.isEmpty(passwordStr)) {
      binding.password.error = context.getString(R.string.error_field_required)
      focusView = binding.password
      cancel = true
    }
    if (TextUtils.isEmpty(usernameStr)) {
      binding.username.error = context.getString(R.string.error_field_required)
      focusView = binding.username
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

    val updateVisibility: (View, Boolean) -> Unit = { view, visible ->
      view.visibility = if (visible) View.VISIBLE else View.GONE
      view.animate()
        .setDuration(shortAnimTime)
        .alpha((if (visible) 1 else 0).toFloat())
        .setListener(object : AnimatorListenerAdapter() {
          override fun onAnimationEnd(animation: Animator) {
            view.visibility = if (visible) View.VISIBLE else View.GONE
          }
        })
    }

    updateVisibility(binding.usernameLayout, !show)
    updateVisibility(binding.passwordLayout, !show)
    updateVisibility(binding.loginProgress, show)
  }
}
