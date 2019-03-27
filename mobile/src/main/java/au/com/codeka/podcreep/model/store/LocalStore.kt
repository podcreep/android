package au.com.codeka.podcreep.model.store

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * LocalStore wraps a sqlite database where we store various object locally as a cache for the
 * server.
 */
@Database(
    entities = [Subscription::class, Podcast::class],
    version = 2,
    exportSchema = false)
abstract class LocalStore : RoomDatabase() {
  abstract fun subscriptions(): SubscriptionsDao
  abstract fun podcasts(): PodcastsDao
}
