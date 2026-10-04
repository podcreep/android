package com.podcreep.mobile.util

import android.content.Context
import com.podcreep.mobile.R
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.Locale

/**
 * "Humanize" the given date to display just the day. This is not quite the same as "formatting" as
 * the result is not a strict single format. For example, if the given date is today's date, we'll
 * return "today" similarly for "yesterday" for dates less than 5 days ago, we'll return the day of
 * the week. And for dates more than 5 days ago, we'll return a string like "Apr 4".
 */
fun Date.humanizeDay(context: Context): String {
  val localDate = toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
  val today = LocalDate.now()
  if (localDate == today) {
    return context.resources.getString(R.string.today)
  }

  if (localDate == today.minusDays(1)) {
    return context.resources.getString(R.string.yesterday)
  }

  if (localDate.isAfter(today.minusDays(5))) {
    val format = SimpleDateFormat("EEEE", context.resources.configuration.locales.get(0))
    return format.format(this)
  }

  val format = SimpleDateFormat("MMM d", context.resources.configuration.locales.get(0))
  return format.format(this)
}

fun formatSeconds(seconds: Int?): String {
  if (seconds == null || seconds < 0) return "--:--"
  val h = seconds / 3600
  val m = (seconds % 3600) / 60
  val s = seconds % 60
  return if (h > 0) {
    String.format(Locale.getDefault(), "%d:%02d:%02d", h, m, s)
  } else {
    String.format(Locale.getDefault(), "%02d:%02d", m, s)
  }
}

fun formatSeconds(seconds: Long?): String {
  if (seconds == null || seconds < 0) return "--:--"
  val h = seconds / 3600
  val m = (seconds % 3600) / 60
  val s = seconds % 60
  return if (h > 0) {
    String.format(Locale.getDefault(), "%d:%02d:%02d", h, m, s)
  } else {
    String.format(Locale.getDefault(), "%02d:%02d", m, s)
  }
}