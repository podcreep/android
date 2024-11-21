package com.podcreep.mobile.ui.library

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
  data object NewReleases : BottomNavItem("new_releases", Icons.Default.NewReleases, "New Releases")
  data object InProgress : BottomNavItem("in_progress", Icons.Default.PlayCircle, "In Progress")
  data object Podcasts : BottomNavItem("podcasts", Icons.Default.Subscriptions, "Podcasts")
}

val routes = listOf(
  BottomNavItem.NewReleases, BottomNavItem.InProgress, BottomNavItem.Podcasts
)

@Composable
fun BottomNavigationBar(navController: NavController) {
  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentDestination = navBackStackEntry?.destination

  NavigationBar {
    routes.forEach { item ->
      NavigationBarItem(
        icon = { Icon(item.icon, contentDescription = item.label, tint = Color.White) },
        label = { Text(item.label) },
        selected = currentDestination?.hierarchy?.any { it.route == item.route } ?: false,
        onClick = { navController.navigate(item.route) }
      )
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionsScreen() {
  val navController = rememberNavController()
  val sheetState = rememberStandardBottomSheetState()

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    bottomBar = { BottomNavigationBar(navController = navController) }
  ) { paddingValues  ->
    NavHost(
      navController,
      modifier = Modifier.padding(paddingValues),
      startDestination = BottomNavItem.NewReleases.route) {

      composable(BottomNavItem.NewReleases.route) { NewReleases() }
      composable(BottomNavItem.InProgress.route) { InProgress() }
      composable(BottomNavItem.Podcasts.route) { SubscribedPodcasts() }
    }
  }
}
