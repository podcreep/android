package com.podcreep.mobile

import android.app.Application
import android.content.Context
import androidx.work.Configuration
import androidx.work.WorkManager
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
class App : Application() {
  @Inject lateinit var syncManager: SyncManager

  @EntryPoint
  @InstallIn(SingletonComponent::class)
  interface WorkerFactoryEntryPoint {
    fun workerFactory(): SyncManager.SyncDelegatingWorkerFactory
  }
  override fun onCreate() {
    super.onCreate()

    val workManagerConfiguration = Configuration.Builder()
      .setWorkerFactory(EntryPoints.get(this, WorkerFactoryEntryPoint::class.java).workerFactory())
      .setMinimumLoggingLevel(android.util.Log.DEBUG)
      .build()
    WorkManager.initialize(this, workManagerConfiguration)

    syncManager.maybeEnqueue()
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
