package au.com.codeka.podcreep.net

import android.os.Build
import au.com.codeka.podcreep.BuildConfig

class Server {
  companion object {
    private var cookie: String? = null

    private fun isEmulator(): Boolean {
      return Build.FINGERPRINT.startsWith("generic")
          || Build.FINGERPRINT.startsWith("unknown")
          || Build.MODEL.contains("google_sdk")
          || Build.MODEL.contains("Emulator")
          || Build.MODEL.contains("Android SDK built for x86")
          || Build.MANUFACTURER.contains("Genymotion")
          || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
          || "google_sdk" == Build.PRODUCT
    }

    private fun url(url: String): String {
      return when {
        isEmulator() -> "http://10.0.2.2:8080$url"
        BuildConfig.DEBUG -> "http://127.0.0.1:8080$url"
        else -> "https://podcreep.appspot.com$url"
      }
    }

    fun updateCookie(newCookie: String?) {
      cookie = newCookie
    }

    fun request(url: String): HttpRequest.Builder {
      val builder = HttpRequest.Builder()
          .url(url(url))
      if (cookie != null) {
        builder.headers["Authorization"] = "Bearer $cookie"
      }
      return builder
    }
  }
}
