package com.podcreep.mobile.data

import com.podcreep.mobile.data.local.Setting
import com.podcreep.mobile.data.local.SettingDao
import javax.inject.Inject

class SettingsRepository @Inject constructor(
    private val settingDao: SettingDao,
) {
  /** Gets a setting with a string value. */
  suspend fun getString(name: String, defaultValue: String): String {
    val setting = settingDao.get(name) ?: return defaultValue
    val value = setting.stringValue ?: return defaultValue
    return value
  }
  /** Gets a setting with a string value. */
  suspend fun getInt(name: String, defaultValue: Long): Long {
    val setting = settingDao.get(name) ?: return defaultValue
    val value = setting.intValue ?: return defaultValue
    return value
  }

  suspend fun setValue(name: String, value: String) {
    settingDao.save(Setting(name = name, stringValue = value, intValue = null))
  }

  suspend fun setValue(name: String, value: Long) {
    settingDao.save(Setting(name = name, stringValue = null, intValue = value))
  }
}
