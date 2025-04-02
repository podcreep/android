package com.podcreep.mobile.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DrawerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.podcreep.mobile.BuildConfig
import com.podcreep.mobile.R

@Composable
fun PodcreepDrawer(viewModel: PodcreepDrawerViewModel = hiltViewModel()) {
    ModalDrawerSheet {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())) {

            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.align(alignment = Alignment.CenterHorizontally)) {
                Image(
                    painter = painterResource(id = R.drawable.podcreep),
                    contentDescription = "Podcreep logo"
                )
            }
            Text(
                "Podcreep",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.titleLarge)
            Text(
                "Version: " + BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")W",
                modifier = Modifier.padding(16.dp)
            )
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
}
