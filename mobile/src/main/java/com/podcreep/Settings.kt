package com.podcreep

import android.content.Context
import android.content.SharedPreferences
import java.util.*

class Settings(context: Context) {

  enum class ValueType {
    INT,
    STRING,
    BOOLEAN,
    DATE,
  }

  companion object Keys {
    /** Our cookie that we use to authenticate with the server. */
    val COOKIE = Key("cookie", ValueType.STRING)

    /** The last time we sync'd with the server. */
    val LAST_SYNC_TIME = Key("last_sync_type", ValueType.DATE)

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

  private var preferences: SharedPreferences = context.getSharedPreferences("prefs", 0)

  fun getString(key: Key): String {
    return preferences.getString(key.name(), "")!!
  }

  fun getInt(key: Key): Int {
    return preferences.getInt(key.name(), 0)
  }

  fun getBoolean(key: Key): Boolean {
    return preferences.getBoolean(key.name(), false)
  }

  fun getDate(key: Key): Date {
    val ts = preferences.getLong(key.name(), 0)
    return Date(ts)
  }

  fun put(key: Key, value: String) {
    preferences.edit().putString(key.name(), value).apply()
  }

  fun put(key: Key, value: Date) {
    preferences.edit().putLong(key.name(), value.time).apply()
  }

  inline fun <reified T> get(key: Key): T {
    return when(key.valueType()) {
      ValueType.STRING -> getString(key) as T
      ValueType.INT -> getInt(key) as T
      ValueType.BOOLEAN -> getBoolean(key) as T
      ValueType.DATE -> getDate(key) as T
    }
  }

  class Key(private var name: String, private var valueType: ValueType) {
    fun name(): String { return this.name }
    fun valueType(): ValueType { return this.valueType }
  }
}
