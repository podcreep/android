package au.com.codeka.podcreep.net

import au.com.codeka.podcreep.welcome.LoginRequest
import au.com.codeka.podcreep.welcome.LoginResponse
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import okio.Buffer
import okio.BufferedSource
import okio.Okio
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
  enum class Method {
    GET,
    POST,
    PUT,
    DELETE
  }

  fun execute(): BufferedSource {
    try {
      val conn = URL(url).openConnection() as HttpURLConnection
      conn.requestMethod = method.toString()
      for (key in headers.keys) {
        conn.setRequestProperty(key, headers[key])
      }
      if (body != null) {
        conn.setRequestProperty("Content-Length", Integer.toString(body.size))
        conn.outputStream.write(body)
      }

      if (conn.responseCode != 200) {
        throw HttpException(conn.responseCode, conn.responseMessage)
      }

      return Okio.buffer(Okio.source(conn.inputStream))
    } catch (e: IOException) {
      throw HttpException(e)
    }
  }

  inline fun <reified T> execute(): T {
    val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    return moshi
        .adapter(T::class.java)
        .fromJson(execute())!!
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