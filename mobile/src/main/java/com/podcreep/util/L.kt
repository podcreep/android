package com.podcreep.util

import android.util.Log

/** Wrapper around logging to add some nice stuff. */
class L(private val tag: String) {
  companion object {
    private const val GLOBAL_TAG = "podcreep"
  }

  fun debug(msg: String) {
    Log.d(GLOBAL_TAG, String.format("%s: %s", tag, msg))
  }
  fun debug(msg: String, vararg params: Any?) {
    debug(String.format(msg, *params))
  }

  fun info(msg: String) {
    Log.i(GLOBAL_TAG, String.format("%s: %s", tag, msg))
  }
  fun info(msg: String, vararg params: Any?) {
    info(String.format(msg, *params))
  }

  fun warning(msg: String) {
    Log.w(GLOBAL_TAG, String.format("%s: %s", tag, msg))
  }
  fun warning(msg: String, vararg params: Any?) {
    warning(String.format(msg, *params))
  }
}