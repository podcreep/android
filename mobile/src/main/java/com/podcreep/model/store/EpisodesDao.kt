package com.podcreep.model.store

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface EpisodesDao {
  companion object {
    const val MAX_RESULT_SIZE = 100
  }

  @Query("SELECT * FROM episodes WHERE podcastID=:podcastID ORDER BY pubDate DESC")
  fun get(podcastID: Long): LiveData<List<Episode>>

  @Query("SELECT * FROM episodes WHERE id=:episodeID AND podcastID=:podcastID")
  fun get(podcastID: Long, episodeID: Long): LiveData<Episode>

  @Query("SELECT * FROM episodes WHERE position IS NOT NULL AND position > 0 ORDER BY pubDate DESC LIMIT $MAX_RESULT_SIZE")
  fun getInProgress(): LiveData<List<Episode>>

  @Query("SELECT * FROM episodes WHERE position IS NULL ORDER BY pubDate DESC LIMIT $MAX_RESULT_SIZE")
  fun getNewEpisodes(): LiveData<List<Episode>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insert(vararg episodes: Episode)
}
