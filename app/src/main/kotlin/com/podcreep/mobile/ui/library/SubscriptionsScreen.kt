package com.podcreep.mobile.ui.library

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun SubscriptionsScreen(drawerState: DrawerState) {
  val navController = rememberNavController()
  val pagerState = rememberPagerState(initialPage = 0) { topLevelNavItems.size }

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    topBar = { TopBar(drawerState, navController, pagerState) },
  ) { paddingValues ->
    NavHost(
      navController = navController,
      modifier = Modifier.padding(paddingValues),
      startDestination = NavItem.NewReleases(),
      enterTransition = {
        slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300))
      },
      exitTransition = {
        slideOutHorizontally(targetOffsetX = { -it / 3 }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
      },
      popEnterTransition = {
        slideInHorizontally(initialOffsetX = { -it / 3 }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300))
      },
      popExitTransition = {
        slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
      }
    ) {

      composable<NavItem.NewReleases> {
        TopLevelLibraryPager(pagerState = pagerState, navController = navController)
      }
      composable<NavItem.InProgress> {
        TopLevelLibraryPager(pagerState = pagerState, navController = navController)
      }
      composable<NavItem.Podcasts> {
        TopLevelLibraryPager(pagerState = pagerState, navController = navController)
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

@Composable
private fun TopLevelLibraryPager(
  pagerState: PagerState,
  navController: NavController,
) {
  HorizontalPager(
    state = pagerState,
    modifier = Modifier.fillMaxSize(),
  ) { page ->
    when (page) {
      0 -> NewReleases(onEpisodeDetailsClick = { podcastID, episodeID ->
        navController.navigate(NavItem.EpisodeDetails(podcastID, episodeID))
      })
      1 -> InProgress(onEpisodeDetailsClick = { podcastID, episodeID ->
        navController.navigate(NavItem.EpisodeDetails(podcastID, episodeID))
      })
      2 -> SubscribedPodcasts(onPodcastDetailsClick = { podcastID ->
        navController.navigate(NavItem.PodcastDetails(podcastID))
      })
    }
  }
}
