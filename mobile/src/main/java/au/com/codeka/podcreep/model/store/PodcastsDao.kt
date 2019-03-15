package au.com.codeka.podcreep.model.store

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface PodcastsDao {
  @Query("SELECT * FROM podcasts")
  fun get(): LiveData<List<PodcastEntity>>

  @Delete
  fun delete(vararg podcasts: PodcastEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insert(vararg podcasts: PodcastEntity)
}
