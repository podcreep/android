package com.podcreep.app.welcome

import android.content.Context
import android.widget.RelativeLayout
import com.podcreep.R
import kotlinx.android.synthetic.main.welcome.view.*

class WelcomeLayout(context: Context, var callbacks: Callbacks) : RelativeLayout(context) {
  interface Callbacks {
    fun onLoginClick()
    fun onRegisterClick()
  }

  init {
    inflate(context, R.layout.welcome, this)
    login.setOnClickListener { callbacks.onLoginClick() }
    register.setOnClickListener { callbacks.onRegisterClick() }
  }
}
