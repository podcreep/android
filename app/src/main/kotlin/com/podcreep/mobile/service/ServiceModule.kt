package com.podcreep.mobile.service

import android.app.Service
import com.podcreep.mobile.data.SettingsRepository
import com.podcreep.mobile.data.SubscriptionsRepository
import com.podcreep.mobile.domain.cache.EpisodeMediaCache
import com.podcreep.mobile.domain.cache.PodcastIconCache
import com.podcreep.mobile.domain.sync.PlaybackStateSyncer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.scopes.ServiceScoped

@Module
@InstallIn(ServiceComponent::class)
class ServiceModule {
  @ServiceScoped
  @Provides
  fun provideMediaManager(service: Service,
                          mediaCache: EpisodeMediaCache,
                          iconCache: PodcastIconCache,
                          playbackStateSyncer: PlaybackStateSyncer,
                          subscriptionsRepository: SubscriptionsRepository,
                          settingsRepository: SettingsRepository
  ) = MediaManager(
    service, mediaCache, iconCache, playbackStateSyncer,
    subscriptionsRepository, settingsRepository)
}
