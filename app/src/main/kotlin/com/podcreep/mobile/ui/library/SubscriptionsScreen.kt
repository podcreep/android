package com.podcreep.mobile.ui.library

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun SubscriptionsScreen(drawerState: DrawerState) {
  val navController = rememberNavController()

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    topBar = { TopBar(drawerState, navController) }
  ) { paddingValues  ->
    NavHost(
      navController,
      modifier = Modifier.padding(paddingValues),
      startDestination = NavItem.NewReleases()) {

      composable<NavItem.NewReleases> {
        NewReleases(onEpisodeDetailsClick = { podcastID, episodeID ->
          navController.navigate(NavItem.EpisodeDetails(podcastID, episodeID))
        })
      }
      composable<NavItem.InProgress> {
        InProgress(onEpisodeDetailsClick = { podcastID, episodeID ->
          navController.navigate(NavItem.EpisodeDetails(podcastID, episodeID))
        })
      }
      composable<NavItem.Podcasts> {
        SubscribedPodcasts(onPodcastDetailsClick = { podcastID ->
          navController.navigate(NavItem.PodcastDetails(podcastID))
        })
      }
      composable<NavItem.EpisodeDetails> {
        EpisodeDetails()
      }
      composable<NavItem.PodcastDetails> {
        PodcastDetails(onEpisodeDetailsClick = { podcastID, episodeID ->
          navController.navigate(NavItem.EpisodeDetails(podcastID, episodeID))
        })
      }
    }
  }
}
