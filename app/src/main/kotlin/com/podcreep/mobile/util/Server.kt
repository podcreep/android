package com.podcreep.mobile.util

import android.os.Build
import com.podcreep.mobile.BuildConfig
import com.podcreep.mobile.Settings
import com.podcreep.mobile.util.Server.Companion.JSON
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resumeWithException

@Singleton
class Server @Inject constructor(settings: Settings) {
  // TODO: inject this?
  private val client: OkHttpClient = OkHttpClient()

  private val log = L("Server")

  private var cookie: String? = settings.getString(Settings.COOKIE)

  val isLoggedIn = MutableStateFlow(!cookie.isNullOrEmpty())

  companion object {
    val JSON: MediaType = "application/json".toMediaType()

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

    fun url(url: String): String {
      return when {
        isEmulator() -> "http://10.0.2.2:8080$url"
        //BuildConfig.DEBUG -> "http://127.0.0.1:8080$url"
        else -> "https://podcreep.com$url"
      }
    }
  }

  fun updateCookie(newCookie: String?) {
    cookie = newCookie
    isLoggedIn.value = !cookie.isNullOrEmpty()
  }

  fun request(url: String): Request.Builder {
    return Request.Builder().url(url(url))
  }

  fun call(request: Request.Builder): Call {
    if (cookie != null) {
      request.header("Authorization", "Bearer $cookie")
    }

    return client.newCall(request.build())
  }
}

// Helper that lets us call okhttp3 requests in a coroutine.
suspend fun Call.await(): Response {
  // Record the callstack so any errors record where the call was made from.
  val callstack = IOException().apply {
    stackTrace = stackTrace.copyOfRange(1, stackTrace.size)
  }

  return suspendCancellableCoroutine { cont ->
    enqueue(object: Callback {
      private val log = L("Server")

      @ExperimentalCoroutinesApi
      override fun onResponse(call: Call, response: Response) {
        cont.resume(response) {
          response.close()
        }
      }

      override fun onFailure(call: Call, e: IOException) {
        if (cont.isCancelled) return

        callstack.initCause(e)
        cont.resumeWithException(callstack)
      }
    })
  }
}

inline fun <reified T> T.toRequestBody(): RequestBody {
  return MoshiHelper.create()
    .adapter(T::class.java)
    .toJson(this)
    .toByteArray().toRequestBody(JSON)
}

inline fun <reified T> Response.fromJson(): T {
  val body = body
  if (body != null) {
    val value = MoshiHelper.create()
      .adapter(T::class.java)
      .fromJson(body.string())
    if (value != null) {
      return value
    }
  }

  // Return a default-constructed T in case of error.
  return T::class.java.getConstructor(T::class.java).newInstance()
}