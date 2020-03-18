package au.com.codeka.podcreep.model.store

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface EpisodesDao {
  @Query("SELECT * FROM episodes WHERE podcastID=:podcastID ORDER BY pubDate DESC")
  fun get(podcastID: Long): LiveData<List<Episode>>

  @Query("SELECT * FROM episodes WHERE id=:episodeID AND podcastID=:podcastID")
  fun get(podcastID: Long, episodeID: Long): LiveData<Episode>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insert(vararg episodes: Episode)
}
