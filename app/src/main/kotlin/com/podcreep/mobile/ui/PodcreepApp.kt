package com.podcreep.mobile.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
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
import com.podcreep.mobile.ui.auth.LoginScreen
import com.podcreep.mobile.ui.library.SubscriptionsScreen
import com.podcreep.mobile.util.L

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PodcreepApp(viewModel: PodcreepAppViewModel = hiltViewModel()) {
  val L = L("DEANH")

  if (viewModel.isLoggedIn.collectAsState().value) {
    viewModel.maybeSync()

    ModalNavigationDrawer(
      drawerContent = {
        ModalDrawerSheet {
          Text("Podcreep Drawer", modifier = Modifier.padding(16.dp))
          HorizontalDivider()
          NavigationDrawerItem(
            label = { Text(text = "Drawer Item") },
            selected = false,
            onClick = { /*TODO*/ }
          )
          HorizontalDivider()
          NavigationDrawerItem(
            label = { Text(text = "Log out") },
            selected = false,
            onClick = { viewModel.logout() }
          )
          // ...other drawer items
        }
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
        SubscriptionsScreen()
      }
    }
  } else {
    LoginScreen()
  }
}

