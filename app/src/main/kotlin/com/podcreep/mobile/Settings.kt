package com.podcreep.mobile

import android.content.Context
import android.content.SharedPreferences
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import javax.inject.Inject

class Settings @Inject constructor(appContext: Context) {

  enum class ValueType {
    INT,
    STRING,
    BOOLEAN,
    LOCAL_DATE,
    LOCAL_DATE_TIME,
  }

  companion object Keys {
    /** Our cookie that we use to authenticate with the server. */
    val COOKIE = Key("cookie", ValueType.STRING)

    /** The last time we sync'd with the server. */
    val LAST_SYNC_TIME = Key("last_sync_type", ValueType.LOCAL_DATE_TIME)

    /**
     * The WorkItemID for our worker that syncs. We keep track of this to ensure our work item
     * is always properly scheduled.
     */
    val SYNC_WORK_ID = Key("sync_work_id", ValueType.STRING)

    /**
     * The playback state that we have stored and not yet synced to the server. Once it's synced to
     * the server, we'll remove it from here. {@see PlaybackStateSyncer}.
     */
    val PLAYBACK_STATE_TO_SYNC = Key("playback_state_to_sync", ValueType.STRING)
  }

  private var preferences: SharedPreferences = appContext.getSharedPreferences("prefs", 0)

  fun getString(key: Key): String {
    return preferences.getString(key.name(), "")!!
  }

  fun getInt(key: Key): Int {
    return preferences.getInt(key.name(), 0)
  }

  fun getBoolean(key: Key): Boolean {
    return preferences.getBoolean(key.name(), false)
  }

  fun getLocalDate(key: Key): LocalDate {
    val day = preferences.getLong(key.name(), 0)
    return LocalDate.ofEpochDay(day)
  }

  fun getLocalDateTime(key: Key): LocalDateTime {
    val ts = preferences.getLong(key.name(), 0)
    return LocalDateTime.ofEpochSecond(ts, 0, ZoneOffset.UTC)
  }

  fun put(key: Key, value: String) {
    preferences.edit().putString(key.name(), value).apply()
  }

  fun put(key: Key, value: LocalDate) {
    preferences.edit().putLong(key.name(), value.toEpochDay()).apply()
  }

  fun put(key: Key, value: LocalDateTime) {
    preferences.edit().putLong(key.name(), value.toEpochSecond(ZoneOffset.UTC)).apply()
  }

  inline fun <reified T> get(key: Key): T {
    return when(key.valueType()) {
      ValueType.STRING -> getString(key) as T
      ValueType.INT -> getInt(key) as T
      ValueType.BOOLEAN -> getBoolean(key) as T
      ValueType.LOCAL_DATE -> getLocalDate(key) as T
      ValueType.LOCAL_DATE_TIME -> getLocalDateTime(key) as T
    }
  }

  class Key(private var name: String, private var valueType: ValueType) {
    fun name(): String { return this.name }
    fun valueType(): ValueType { return this.valueType }
  }
}
