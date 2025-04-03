package com.podcreep.mobile.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SettingDao {
  @Query("SELECT * FROM settings WHERE name=:name")
  suspend fun get(name: String): Setting?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun save(vararg setting: Setting)
}
