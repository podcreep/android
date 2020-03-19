package au.com.codeka.podcreep

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
    var COOKIE = Key("cookie", ValueType.STRING)
    var LAST_SYNC_TIME = Key("last_sync_type", ValueType.DATE)
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
