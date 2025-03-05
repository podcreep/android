package com.podcreep.mobile.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.podcreep.mobile.ui.auth.LoginScreen
import com.podcreep.mobile.ui.library.SubscriptionsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PodcreepApp(viewModel: PodcreepAppViewModel = hiltViewModel()) {
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
      BottomSheetScaffold(
        modifier = Modifier.fillMaxSize().padding(bottom = 90.dp),
        sheetPeekHeight = 120.dp,
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

