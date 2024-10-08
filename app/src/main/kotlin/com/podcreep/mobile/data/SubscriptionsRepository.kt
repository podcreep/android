package com.podcreep.mobile.data

import com.podcreep.mobile.data.local.Episode
import com.podcreep.mobile.data.local.EpisodesDao
import com.podcreep.mobile.data.local.Podcast
import com.podcreep.mobile.data.local.PodcastsDao
import com.podcreep.mobile.data.local.Subscription
import com.podcreep.mobile.data.local.SubscriptionsDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class SubscriptionsRepository @Inject constructor(
  private val subscriptionsDao: SubscriptionsDao,
  private val podcastsDao: PodcastsDao,
  private val episodesDao: EpisodesDao,
) {
  /** Gets a list of the subscriptions the user has subscribed to. */
  fun subscriptions(): Flow<List<Subscription>> {
    val subscriptions = subscriptionsDao.get()
    val podcasts = podcastsDao.get()

    return subscriptions.combine(podcasts) { subs, pods ->
      val combinedSubs = arrayListOf<Subscription>()
      for (sub in subs) {
        sub.podcast = null
        for (pod in pods) {
          if (sub.podcastID == pod.id) {
            sub.podcast = pod
          }
        }
        combinedSubs.add(sub)
      }

      combinedSubs
    }
  }

  fun inProgress(): Flow<List<Episode>> {
    return episodesDao.getInProgress()
  }

  fun newEpisodes(): Flow<List<Episode>> {
    return episodesDao.getNewEpisodes()
  }

  fun episodesOf(podcastID: Long): Flow<List<Episode>> {
    return episodesDao.get(podcastID)
  }

  fun podcast(podcastID: Long): Flow<Podcast> {
    return podcastsDao.get(podcastID)
  }

  suspend fun updateEpisode(episode: Episode) {
    return episodesDao.insert(episode)
  }

  suspend fun syncPodcast(podcast: Podcast) {
    podcastsDao.insert(podcast)
  }

  suspend fun syncSubscription(subscription: Subscription) {
    subscriptionsDao.insert(subscription)
  }

  suspend fun syncEpisode(episode: Episode) {
    episodesDao.insert(episode)
  }
}
