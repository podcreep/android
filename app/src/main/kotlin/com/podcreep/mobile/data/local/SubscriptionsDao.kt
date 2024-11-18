package com.podcreep.mobile.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow


@Dao
interface SubscriptionsDao {
  @Query("SELECT * FROM sub_podcasts")
  fun get(): Flow<List<Subscription>>

  @Delete
  suspend fun delete(vararg subscriptions: Subscription)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(vararg subscriptions: Subscription)

  /*
  @Transaction
  fun updateAll(sub_podcasts: List<Subscription>) {
    val existing = get().value

    val toDelete = ArrayList<Subscription>()
    existing?.forEach {
      if (!sub_podcasts.contains(it)) {
        toDelete.add(it)
      }
    }
    if (toDelete.size > 0) {
      delete(*toDelete.toTypedArray())
    }
    insert(*sub_podcasts.toTypedArray())
  }
  */

  @Query("DELETE FROM sub_podcasts")
  suspend fun deleteAll()
}
