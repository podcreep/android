package com.podcreep.mobile.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.podcreep.mobile.ui.auth.LoginScreen
import com.podcreep.mobile.ui.library.SubscriptionsScreen

@Composable
fun PodcreepApp(viewModel: PodcreepAppViewModel = hiltViewModel()) {
  if (viewModel.isLoggedIn.collectAsState().value) {
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
          // ...other drawer items
        }
      }
    ) {
      SubscriptionsScreen()
    }
  } else {
    LoginScreen()
  }
}