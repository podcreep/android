package au.com.codeka.podcreep.model.store

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface EpisodesDao {
  @Query("SELECT * FROM episodes WHERE podcastID=:podcastID")
  fun get(podcastID: Long): LiveData<List<Episode>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insert(vararg episodes: Episode)
}
