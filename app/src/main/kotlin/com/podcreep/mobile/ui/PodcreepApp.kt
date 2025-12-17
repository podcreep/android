package com.podcreep.mobile.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.podcreep.mobile.R
import com.podcreep.mobile.ui.auth.LoginScreen
import com.podcreep.mobile.ui.library.SubscriptionsScreen
import com.podcreep.mobile.ui.settings.SettingsScreen
import com.podcreep.mobile.util.currentFraction

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
    var totalHeight by remember { mutableFloatStateOf(0F) }

    ModalNavigationDrawer(
      drawerState = drawerState,
      drawerContent = {
        PodcreepDrawer(navController, drawerState)
      },
      modifier = Modifier.onGloballyPositioned { coords ->
        totalHeight = coords.size.height.toFloat()
      }
    ) {
      val bottomSheetState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState())
      val hideBottomSheet = viewModel.hideBottomSheet.collectAsState(true)

      val radius = (30f * (bottomSheetState.currentFraction(totalHeight))).dp

      BottomSheetScaffold(
        modifier = Modifier
          .fillMaxSize()
          .padding(bottom = if (hideBottomSheet.value) 0.dp else 90.dp),
        sheetPeekHeight = if (hideBottomSheet.value) 0.dp else 120.dp,
        scaffoldState = bottomSheetState,
        sheetShape = RoundedCornerShape(topStart = radius, topEnd = radius),
        sheetContent = {
          NowPlayingView()
        },
        sheetDragHandle = {},  // No drag handle
      ) { paddingValues  ->
        NavHost(
          navController,
          startDestination = TopLevelItem.Subscriptions.route) {
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

