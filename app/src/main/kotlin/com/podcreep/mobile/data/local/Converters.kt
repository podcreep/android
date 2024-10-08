package com.podcreep.mobile.data.local

import androidx.room.TypeConverter
import java.util.*

class Converters {
  companion object {
    @TypeConverter
    @JvmStatic
    fun fromTimestamp(value: Long?): Date? {
      return if (value == null) null else Date(value)
    }

    @TypeConverter
    @JvmStatic
    fun dateToTimestamp(date: Date?): Long? {
      return date?.time
    }
  }
}
