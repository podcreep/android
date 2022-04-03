package com.podcreep.app.welcome

import android.content.Context
import android.view.LayoutInflater
import android.widget.RelativeLayout
import com.podcreep.databinding.WelcomeBinding

class WelcomeLayout(context: Context, var callbacks: Callbacks) : RelativeLayout(context) {
  interface Callbacks {
    fun onLoginClick()
    fun onRegisterClick()
  }

  init {
    val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    val binding = WelcomeBinding.inflate(layoutInflater, this)
    binding.login.setOnClickListener { callbacks.onLoginClick() }
    binding.register.setOnClickListener { callbacks.onRegisterClick() }
  }
}
