package com.podcreep.util

import androidx.annotation.Keep
import com.squareup.moshi.FromJson
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.text.SimpleDateFormat
import java.util.*

class DateAdapter {
  companion object {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.ENGLISH)
  }

  @ToJson
  fun toJson(dt: Date): String {
    return dateFormat.format(dt)
  }

  @FromJson
  fun fromJson(str: String): Date {
    return dateFormat.parse(str) ?: Date(0)
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