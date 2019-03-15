package au.com.codeka.podcreep.model.store

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * LocalStore wraps a sqlite database where we store various object locally as a cache for the
 * server.
 */
@Database(
    entities = [SubscriptionEntity::class, PodcastEntity::class],
    version = 1,
    exportSchema = false)
abstract class LocalStore : RoomDatabase() {
  abstract fun subscriptionsDao(): SubscriptionsDao
}
