package com.podcreep.mobile.ui.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavController
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


@Composable
fun TopBarNavigationMenu(navController: NavController) {
  val navDropDownExpanded = remember { mutableStateOf(false) }

  val currentNavItem = navController.currentBackStackEntryFlow.map {
    it.toRoute<NavItem>()
  }.collectAsState(NavItem.NewReleases())

  if (currentNavItem.value.isTopLevel) {
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
          leadingIcon = { Icon(painter = painterResource(item.iconResId), contentDescription = item.label, tint = Color.White) },
          onClick = {
            navController.navigate(item)
            navDropDownExpanded.value = false
          }
        )
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(drawerState: DrawerState, navController: NavController) {
  val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
  val scope = rememberCoroutineScope()

  val currentNavItem = navController.currentBackStackEntryFlow.map {
    it.toRoute<NavItem>()
  }.collectAsState(NavItem.NewReleases())

  CenterAlignedTopAppBar(
    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
      containerColor = MaterialTheme.colorScheme.primaryContainer,
      titleContentColor = MaterialTheme.colorScheme.primary,
    ),
    title = { TopBarNavigationMenu(navController) },
    navigationIcon = {
      if (!currentNavItem.value.isTopLevel) {
        IconButton(onClick = { navController.navigateUp() }) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back"
          )
        }
      } else {
        IconButton(onClick = {
          scope.launch {
            drawerState.apply {
              if (isClosed) open() else close()
            }
          }
        }) {
          Icon(
            imageVector = Icons.Filled.Menu,
            contentDescription = "Menu"
          )
        }
      }
    },
    actions = {
      IconButton(onClick = { /* do something */ }) {
        Icon(
          imageVector = Icons.Filled.Person,
          contentDescription = "Profile"
        )
      }
    },
    scrollBehavior = scrollBehavior,
  )
}
