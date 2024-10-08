package com.podcreep.mobile.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PodcastsDao {
  @Query("SELECT * FROM podcasts")
  fun get(): Flow<List<Podcast>>

  @Query("SELECT * FROM podcasts WHERE id=:id")
  fun get(id: Long): Flow<Podcast>

  @Query("SELECT * FROM podcasts")
  fun getSync(): Flow<List<Podcast>>

  @Delete
  suspend fun delete(vararg podcasts: Podcast)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(vararg podcasts: Podcast)
}
