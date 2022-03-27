package com.podcreep.model.store

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * LocalStore wraps a sqlite database where we store various object locally as a cache for the
 * server.
 */
@Database(
    entities = [Subscription::class, Podcast::class, Episode::class],
    version = 6,
    exportSchema = false)
@TypeConverters(Converters::class)
abstract class LocalStore : RoomDatabase() {
  abstract fun subscriptions(): SubscriptionsDao
  abstract fun podcasts(): PodcastsDao
  abstract fun episodes(): EpisodesDao
}
