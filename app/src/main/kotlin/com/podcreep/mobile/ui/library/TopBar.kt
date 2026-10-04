package com.podcreep.mobile.ui.library

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavController
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@Composable
fun TopBarTabs(pagerState: PagerState) {
  val scope = rememberCoroutineScope()
  TabRow(
    selectedTabIndex = pagerState.currentPage,
    containerColor = MaterialTheme.colorScheme.primaryContainer,
    contentColor = MaterialTheme.colorScheme.primary,
  ) {
    topLevelNavItems.forEachIndexed { index, item ->
      Tab(
        selected = pagerState.currentPage == index,
        onClick = {
          scope.launch {
            pagerState.animateScrollToPage(index)
          }
        },
        text = {
          Text(
            text = item.label,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        },
        icon = {
          Icon(
            painter = painterResource(item.iconResId),
            contentDescription = item.label
          )
        }
      )
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
  drawerState: DrawerState,
  navController: NavController,
  pagerState: PagerState
) {
  val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
  val scope = rememberCoroutineScope()

  val currentNavItem = remember(navController) {
    navController.currentBackStackEntryFlow.map {
      it.toRoute<NavItem>()
    }
  }.collectAsState(NavItem.NewReleases())

  Column {
    CenterAlignedTopAppBar(
      colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        titleContentColor = MaterialTheme.colorScheme.primary,
      ),
      title = {
        if (currentNavItem.value.isTopLevel) {
          Text(
            topLevelNavItems[pagerState.currentPage].label,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        } else {
          Text(
            currentNavItem.value.label,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      },
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

    if (currentNavItem.value.isTopLevel) {
      TopBarTabs(pagerState = pagerState)
    }
  }
}
