package com.podcreep.mobile.ui.library

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun SubscriptionsScreen() {
  val navController = rememberNavController()

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    topBar = { TopBar(navController) }
  ) { paddingValues  ->
    NavHost(
      navController,
      modifier = Modifier.padding(paddingValues),
      startDestination = NavItem.NewReleases()) {

      composable<NavItem.NewReleases> {
        NewReleases(onPodcastDetailsClick = { podcastID, episodeID ->
          navController.navigate(NavItem.EpisodeDetails(podcastID, episodeID))
        })
      }
      composable<NavItem.InProgress> {
        InProgress(onPodcastDetailsClick = { podcastID, episodeID ->
          navController.navigate(NavItem.EpisodeDetails(podcastID, episodeID))
        })
      }
      composable<NavItem.Podcasts> {
        SubscribedPodcasts()
      }
      composable<NavItem.EpisodeDetails> {
        EpisodeDetails()
      }
    }
  }
}
