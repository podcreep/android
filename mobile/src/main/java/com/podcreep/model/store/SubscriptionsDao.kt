package com.podcreep.model.store

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.Transaction

@Dao
interface SubscriptionsDao {
  @Query("SELECT * FROM sub_podcasts")
  fun get(): LiveData<List<Subscription>>

  @Delete
  fun delete(vararg subscriptions: Subscription)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insert(vararg subscriptions: Subscription)

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
}
