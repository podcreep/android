package com.podcreep.mobile.data.local

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Store is a class we use for local storage of sub_podcasts, episode podcast_details, and all that
 * stuff. This class also manages requesting from the server for updates to this data as required.
 * You should use the {@link LiveData} methods to make sure you're always displaying the most
 * up-to-date data.
 */
@Module
@InstallIn(SingletonComponent::class)
object LocalStoreModule {
  @Singleton
  @Provides
  fun provideLocalStore(@ApplicationContext context: Context): LocalStore = Room
      .databaseBuilder(context, LocalStore::class.java, "local-store")
      // We'll just allow database upgrades to drop the data store, since it's just a cache of the
      // server anyway, it shouldn't be a big deal.
      .fallbackToDestructiveMigration()
      .build()

  @Singleton
  @Provides
  fun provideSubscriptionsDao(localStore: LocalStore): SubscriptionsDao {
    return localStore.subscriptions()
  }

  @Singleton
  @Provides
  fun providePodcastsDao(localStore: LocalStore): PodcastsDao {
    return localStore.podcasts()
  }

  @Singleton
  @Provides
  fun provideEpisodesDao(localStore: LocalStore): EpisodesDao {
    return localStore.episodes()
  }

  @Singleton
  @Provides
  fun provideSettingDao(localStore: LocalStore): SettingDao {
    return localStore.settings()
  }
}
