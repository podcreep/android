package com.podcreep.mobile.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow


@Dao
interface EpisodesDao {
  companion object {
    const val MAX_RESULT_SIZE = 100
  }

  @Query("SELECT * FROM episodes WHERE podcastID=:podcastID ORDER BY pubDate DESC")
  fun get(podcastID: Long): Flow<List<Episode>>

  @Query("SELECT * FROM episodes WHERE id=:episodeID AND podcastID=:podcastID")
  fun get(podcastID: Long, episodeID: Long): Flow<Episode>

  @Query("""
    SELECT *
    FROM episodes
    WHERE position IS NOT NULL
      AND position > 0
    ORDER BY lastListenTime DESC, pubDate DESC
    LIMIT $MAX_RESULT_SIZE
  """)
  fun getInProgress(): Flow<List<Episode>>

  @Query("""
    SELECT *
    FROM episodes
    WHERE position IS NULL
    ORDER BY pubDate DESC
    LIMIT $MAX_RESULT_SIZE
  """)
  fun getNewEpisodes(): Flow<List<Episode>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(vararg episodes: Episode)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertSync(vararg episodes: Episode)

  @Query("DELETE FROM episodes")
  suspend fun deleteAll()
}
