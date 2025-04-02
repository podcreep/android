package com.podcreep.mobile.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DrawerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.podcreep.mobile.BuildConfig
import com.podcreep.mobile.R
import kotlinx.coroutines.launch

@Composable
fun DrawerItem(item: TopLevelItem, selected: Boolean, onItemClick: (TopLevelItem) -> Unit) {
    val background = if (selected) R.color.purple_200 else android.R.color.transparent
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = { onItemClick(item) })
            .height(45.dp)
            .background(colorResource(id = background))
            .padding(start = 10.dp)
    ) {
        Image(
            painter = painterResource(id = item.icon),
            contentDescription = item.title,
            colorFilter = ColorFilter.tint(Color.White),
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .height(35.dp)
                .width(35.dp)
        )
        Spacer(modifier = Modifier.width(7.dp))
        Text(
            text = item.title,
            fontSize = 18.sp,
            color = Color.White
        )
    }
}

@Composable
fun PodcreepDrawer(
    navController: NavController,
    drawerState: DrawerState,
    viewModel: PodcreepDrawerViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    val topLevelItems = listOf(
        TopLevelItem.Subscriptions,
        TopLevelItem.Settings,
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

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

            HorizontalDivider(
                modifier = Modifier.padding(16.dp)
            )

            topLevelItems.forEach { item ->
                DrawerItem(item = item, selected = currentRoute == item.route, onItemClick = {
                    navController.navigate(item.route) {
                        navController.graph.startDestinationRoute?.let { route ->
                            popUpTo(route) {
                                saveState = true
                            }
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                    scope.launch {
                        drawerState.close()
                    }
                })
            }

            HorizontalDivider(
                modifier = Modifier.padding(16.dp)
            )

            NavigationDrawerItem(
                label = { Text(text = "Log out") },
                selected = false,
                onClick = { viewModel.logout() }
            )
            // ...other drawer items
        }
    }
}
