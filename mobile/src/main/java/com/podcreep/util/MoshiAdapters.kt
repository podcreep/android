package com.podcreep.util

import com.squareup.moshi.FromJson
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class DateAdapter {
  companion object {
    private val dateFormats = arrayOf(
      SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX", Locale.ENGLISH),
      SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.ENGLISH),
    )
  }

  @ToJson
  fun toJson(dt: Date): String {
    return dateFormats[0].format(dt)
  }

  @FromJson
  fun fromJson(str: String): Date {
    for (dateFormat in dateFormats) {
      try {
        return dateFormat.parse(str) ?: Date(0)
      } catch (e: ParseException) {
        // Keep trying.
      }
    }
    return Date(0)
  }
}

object MoshiHelper {
  // TODO: should we cache the Moshi object? Or re-create it every time?
  fun create(): Moshi {
    return Moshi.Builder()
      .add(KotlinJsonAdapterFactory())
      .add(DateAdapter())
      .build()
  }
}