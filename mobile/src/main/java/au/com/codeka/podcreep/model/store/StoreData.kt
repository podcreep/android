package au.com.codeka.podcreep.model.store

import java.util.*

/**
 * StoreData represents something that we want to store in our local cache, and also be able to
 * refresh it from the server some how.
 */
class StoreData<T> {
  /**
   * The date that this resource was last fetched from the server.
   */
  var lastServerFetch: Date? = null

  /**
   * The actual data
   */
  private var data: T? = null

  fun toJson(): String {

  }
}
