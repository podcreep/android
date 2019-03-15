package au.com.codeka.podcreep.model.store

import androidx.lifecycle.LiveData
import androidx.room.*
import au.com.codeka.podcreep.model.Subscription
import androidx.room.Transaction

@Dao
interface SubscriptionsDao {
  @Query("SELECT * FROM subscriptions")
  fun get(): LiveData<List<SubscriptionEntity>>

  @Delete
  fun delete(vararg subscriptions: SubscriptionEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insert(vararg subscriptions: SubscriptionEntity)

  @Transaction
  fun updateAll(subscriptions: List<SubscriptionEntity>) {
    val existing = get().value

    val toDelete = ArrayList<SubscriptionEntity>()
    existing?.forEach {
      if (!subscriptions.contains(it)) {
        toDelete.add(it)
      }
    }
    if (toDelete.size > 0) {
      delete(*toDelete.toTypedArray())
    }
    insert(*subscriptions.toTypedArray())
  }
}
