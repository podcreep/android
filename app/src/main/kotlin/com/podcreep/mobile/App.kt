package com.podcreep.mobile

import android.app.Application
import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.podcreep.mobile.service.SyncManager
import dagger.Module
import dagger.Provides
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

@HiltAndroidApp
class App : Application(), Configuration.Provider {
  @Inject lateinit var syncManager: SyncManager

  @EntryPoint
  @InstallIn(SingletonComponent::class)
  interface HiltWorkerFactoryEntryPoint {
    fun workerFactory(): HiltWorkerFactory
  }
  override val workManagerConfiguration = Configuration.Builder()
      .setWorkerFactory(EntryPoints.get(this, HiltWorkerFactoryEntryPoint::class.java).workerFactory())
      .setMinimumLoggingLevel(android.util.Log.DEBUG)
      .build()

  override fun onCreate() {
    super.onCreate()

    syncManager.maybeSync()
  }
}

@Module
@InstallIn(SingletonComponent::class)
class AppModule {
  @Singleton
  @Provides
  fun provideSettings(@ApplicationContext appContext: Context): Settings {
    return Settings(appContext)
  }
}
