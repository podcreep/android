package au.com.codeka.podcreep.model.store

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface PodcastsDao {
  @Query("SELECT * FROM podcasts")
  fun get(): LiveData<List<Podcast>>

  @Query("SELECT * FROM podcasts WHERE id=:id")
  fun get(id: Long): LiveData<Podcast>

  @Query("SELECT * FROM podcasts")
  fun getSync(): List<Podcast>

  @Delete
  fun delete(vararg podcasts: Podcast)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insert(vararg podcasts: Podcast)
}
