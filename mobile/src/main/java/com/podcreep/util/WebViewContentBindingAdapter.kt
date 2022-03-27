package com.podcreep.util

import android.webkit.WebView
import androidx.databinding.BindingAdapter

@BindingAdapter("app:html")
fun WebView.setHtml(html: String) {
  this.loadDataWithBaseURL(null, html, "text/html; charset=utf-8", null, null)
}
