package com.podcreep.mobile.ui.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.PersonPin
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.map

sealed class NavItem(val route: String, val icon: ImageVector, val label: String) {
  data object NewReleases : NavItem("new_releases", Icons.Default.NewReleases, "New Releases")
  data object InProgress : NavItem("in_progress", Icons.Default.PlayCircle, "In Progress")
  data object Podcasts : NavItem("podcasts", Icons.Default.Subscriptions, "Podcasts")
}

val topLevelNavItems = listOf(
  NavItem.NewReleases, NavItem.InProgress, NavItem.Podcasts
)

@Composable
fun TopBarNavigationMenu(navController: NavController) {
  val navDropDownExpanded = remember { mutableStateOf(false) }

  val currentNavItem = navController.currentBackStackEntryFlow.map {
    var currentNavItem: NavItem? = null
    for (route in topLevelNavItems) {
      if ((it.destination.route ?: "") == route.route) {
        currentNavItem = route
      }
    }

    currentNavItem ?: NavItem.NewReleases
  }.collectAsState(NavItem.NewReleases)

  Row(
    modifier = Modifier.clickable {
      navDropDownExpanded.value = !navDropDownExpanded.value
    }) {
    Text(
      currentNavItem.value.label,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
    Icon(
      imageVector = Icons.Filled.KeyboardArrowDown,
      contentDescription = "Change page"
    )
  }
  DropdownMenu(
    expanded = navDropDownExpanded.value,
    onDismissRequest = { navDropDownExpanded.value = false}
  ) {
    topLevelNavItems.forEachIndexed { index, item ->
      if (index > 0) {
        HorizontalDivider()
      }

      DropdownMenuItem(
        text = { Text(text = item.label) },
        leadingIcon = { Icon(item.icon, contentDescription = item.label, tint = Color.White) },
        //label = { Text(item.label) },
        //selected = currentDestination?.hierarchy?.any { it.route == item.route } ?: false,
        onClick = {
          navController.navigate(item.route)
          navDropDownExpanded.value = false
        }
      )
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionsScreen() {
  val navController = rememberNavController()
  val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    topBar = {
      CenterAlignedTopAppBar(
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
          containerColor = MaterialTheme.colorScheme.primaryContainer,
          titleContentColor = MaterialTheme.colorScheme.primary,
        ),
        title = { TopBarNavigationMenu(navController) },
        navigationIcon = {
          IconButton(onClick = { /* do something */ }) {
            Icon(
              imageVector = Icons.Filled.Menu,
              contentDescription = "Localized description"
            )
          }
        },
        actions = {
          IconButton(onClick = { /* do something */ }) {
            Icon(
              imageVector = Icons.Filled.PersonPin,
              contentDescription = "Profile"
            )
          }
        },
        scrollBehavior = scrollBehavior,
      )
    },
  ) { paddingValues  ->
    NavHost(
      navController,
      modifier = Modifier.padding(paddingValues),
      startDestination = NavItem.NewReleases.route) {

      composable(NavItem.NewReleases.route) { NewReleases() }
      composable(NavItem.InProgress.route) { InProgress() }
      composable(NavItem.Podcasts.route) { SubscribedPodcasts() }
    }
  }
}
