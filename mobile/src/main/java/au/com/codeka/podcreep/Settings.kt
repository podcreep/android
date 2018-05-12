package au.com.codeka.podcreep

import android.content.Context
import android.content.SharedPreferences

class Settings(context: Context) {

  enum class ValueType {
    INT,
    STRING,
    BOOLEAN,
  }

  companion object Keys {
    var COOKIE = Key("cookie", ValueType.STRING)
  }

  private var preferences: SharedPreferences = context.getSharedPreferences("prefs", 0)

  fun getString(key: Key): String {
    return preferences.getString(key.name(), "")
  }

  fun getInt(key: Key): Int {
    return preferences.getInt(key.name(), 0)
  }

  fun getBoolean(key: Key): Boolean {
    return preferences.getBoolean(key.name(), false)
  }

  inline fun <reified T> get(key: Key): T {
    return when {
      key.valueType() == ValueType.STRING -> getString(key) as T
      key.valueType() == ValueType.INT -> getInt(key) as T
      key.valueType() == ValueType.BOOLEAN -> getBoolean(key) as T
      else -> throw RuntimeException("Key has unknown ValueType: " + key.valueType())
    }
  }

  class Key(private var name: String, private var valueType: ValueType) {
    fun name(): String { return this.name }
    fun valueType(): ValueType { return this.valueType }
  }
}
