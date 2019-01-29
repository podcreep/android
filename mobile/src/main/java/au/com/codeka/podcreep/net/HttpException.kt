package au.com.codeka.podcreep.net

import java.io.IOException

class HttpException : Exception {
  private val exception: IOException?
  private val status: Int?
  private val msg: String

  constructor(e: IOException): super(e.message) {
    exception = e
    status = 0
    msg = e.toString()
  }

  constructor(status: Int, msg: String): super("$status: $msg") {
    exception = null
    this.status = status
    this.msg = msg
  }

  override fun toString(): String {
    return if (status != 0) {
      "$status: $msg"
    } else {
      exception!!.toString()
    }
  }

  val statusCode: Int?
      get() = this.status
}
