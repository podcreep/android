package com.podcreep.net

import android.util.Log
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okio.BufferedSource
import okio.buffer
import okio.source
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 * Simple wrapper around [HttpURLConnection] that lets us make HTTP requests more easily.
 */
class HttpRequest private constructor(
    private val url: String,
    private val method: Method,
    private val headers: Map<String, String>,
    private val body: ByteArray?) {
  interface ErrorHandler {
    fun onError(e: HttpException)
  }

  companion object {
    const val TAG = "HttpRequest"

    private val globalErrorHandlers = ArrayList<ErrorHandler>()

    fun addGlobalErrorHandler(handler: ErrorHandler) {
      globalErrorHandlers.add(handler)
    }
  }

  private lateinit var conn: HttpURLConnection

  enum class Method {
    GET,
    POST,
    PUT,
    DELETE
  }

  val contentSize: Long
    get() = conn.contentLengthLong

  fun execute(): BufferedSource {
    try {
      conn = URL(url).openConnection() as HttpURLConnection
      conn.requestMethod = method.toString()
      for (key in headers.keys) {
        conn.setRequestProperty(key, headers[key])
      }
      if (body != null) {
        conn.setRequestProperty("Content-Length", body.size.toString())
        conn.outputStream.write(body)
      }

      if (conn.responseCode != 200) {
        throw HttpException(conn.responseCode, conn.responseMessage)
      }

      return conn.inputStream.source().buffer()
    } catch (e: HttpException) {
      for (errorHandler in globalErrorHandlers) {
        errorHandler.onError(e)
      }
      throw e
    } catch (e: IOException) {
      val httpException = HttpException(e)
      for (errorHandler in globalErrorHandlers) {
        errorHandler.onError(httpException)
      }

      throw httpException
    }
  }

  inline fun <reified T> execute(): T {
    val resp = execute()
    val json = resp.readString(Charsets.UTF_8)

    try {
      val moshi = Moshi.Builder()
          .add(KotlinJsonAdapterFactory())
          .build()
      return moshi
          .adapter(T::class.java)
          .fromJson(json)!!
    } catch(e: JsonDataException) {
      Log.e(TAG, json)
      throw e
    }
  }

  class Builder {
    private var url: String? = null
    private var method: Method
    val headers: HashMap<String, String>
    private var body: ByteArray? = null

    init {
      method = Method.GET
      headers = HashMap()
    }

    fun url(url: String): Builder {
      this.url = url
      return this
    }

    fun method(method: Method): Builder {
      this.method = method
      return this
    }

    fun header(name: String, value: String): Builder {
      headers[name] = value
      return this
    }

    fun body(body: ByteArray): Builder {
      this.body = body
      return this
    }

    inline fun <reified T> body(body: T): Builder {
      val moshi = Moshi.Builder()
          .add(KotlinJsonAdapterFactory())
          .build()
      return body(moshi
          .adapter(T::class.java)
          .toJson(body)
          .toByteArray())
    }

    fun build(): HttpRequest {
      if (url == null) {
        throw HttpException(-1, "url is null")
      }

      return HttpRequest(url!!, method, headers, body)
    }
  }
}
