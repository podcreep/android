package com.podcreep.mobile.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.podcreep.mobile.R
import com.podcreep.mobile.ui.auth.LoginScreen
import com.podcreep.mobile.ui.library.SubscriptionsScreen
import com.podcreep.mobile.ui.settings.SettingsScreen
import com.podcreep.mobile.util.L

sealed class TopLevelItem(var route: String, var icon: Int, var title: String) {
  object Subscriptions : TopLevelItem("subscriptions", R.drawable.ic_podcast, "Subscriptions")
  object Settings : TopLevelItem("settings", R.drawable.ic_settings, "Settings")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PodcreepApp(viewModel: PodcreepAppViewModel = hiltViewModel()) {
  if (viewModel.isLoggedIn.collectAsState().value) {
    viewModel.maybeSync()
    val navController = rememberNavController()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    ModalNavigationDrawer(
      drawerState = drawerState,
      drawerContent = {
        PodcreepDrawer(navController, drawerState)
      }
    ) {
      val bottomSheetState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(skipHiddenState = false))
      val hideBottomSheet = viewModel.hideBottomSheet.collectAsState(true)

      BottomSheetScaffold(
        modifier = Modifier.fillMaxSize().padding(bottom = if (hideBottomSheet.value) 0.dp else 90.dp),
        sheetPeekHeight = if (hideBottomSheet.value) 0.dp else 120.dp,
        scaffoldState = bottomSheetState,
        sheetContent = {
          NowPlayingView()
        },
      ) { paddingValues  ->
        NavHost(navController, startDestination = TopLevelItem.Subscriptions.route) {
          composable(TopLevelItem.Subscriptions.route) {
            SubscriptionsScreen(drawerState)
          }
          composable(TopLevelItem.Settings.route) {
            SettingsScreen(drawerState)
          }
        }
      }
    }
  } else {
    LoginScreen()
  }
}

