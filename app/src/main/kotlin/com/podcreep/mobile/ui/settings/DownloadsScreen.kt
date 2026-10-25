package com.podcreep.mobile.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.podcreep.mobile.R

fun formatBytes(bytes: Long): String {
  if (bytes <= 0) return "0 B"
  val kb = 1024.0
  val mb = kb * 1024
  val gb = mb * 1024
  return when {
    bytes >= gb -> "%.2f GB".format(bytes / gb)
    bytes >= mb -> "%.1f MB".format(bytes / mb)
    bytes >= kb -> "%.1f KB".format(bytes / kb)
    else -> "$bytes B"
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsScreen(
  onNavigateUp: () -> Unit,
  viewModel: DownloadsViewModel = hiltViewModel()
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  var showCleanUpDialog by remember { mutableStateOf(false) }

  Scaffold(
    topBar = {
      CenterAlignedTopAppBar(
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
          containerColor = MaterialTheme.colorScheme.primaryContainer,
          titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
          navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
          actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
        title = {
          Text("Downloads")
        },
        navigationIcon = {
          IconButton(onClick = onNavigateUp) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back"
            )
          }
        }
      )
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Text(
            text = "Storage Stats",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          HorizontalDivider()
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("Downloaded Episodes:", style = MaterialTheme.typography.bodyMedium)
            Text(
              text = "${uiState.downloadCount}",
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.Bold
            )
          }
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("Total Space Used:", style = MaterialTheme.typography.bodyMedium)
            Text(
              text = formatBytes(uiState.totalSizeBytes),
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }

      Button(
        onClick = { showCleanUpDialog = true },
        enabled = uiState.downloadCount > 0 && uiState.totalSizeBytes > 0,
        modifier = Modifier.fillMaxWidth()
      ) {
        Icon(
          painter = painterResource(id = R.drawable.ic_download_24dp),
          contentDescription = null,
          modifier = Modifier.padding(end = 8.dp)
        )
        Text("Clean Up Downloads")
      }

      if (uiState.episodes.isNotEmpty()) {
        Text(
          text = "Downloaded Files",
          style = MaterialTheme.typography.titleSmall,
          color = MaterialTheme.colorScheme.primary
        )
        LazyColumn(
          verticalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.weight(1f)
        ) {
          items(uiState.episodes, key = { "${it.podcastId}:${it.episodeId}" }) { item ->
            Card(
              modifier = Modifier.fillMaxWidth(),
              colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
              )
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = item.episodeTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                  )
                  Text(
                    text = item.podcastTitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                  Text(
                    text = formatBytes(item.sizeBytes),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                  )
                }
                IconButton(
                  onClick = { viewModel.deleteSingleDownload(item.podcastId, item.episodeId) }
                ) {
                  Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete download"
                  )
                }
              }
            }
          }
        }
      } else {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "No downloaded episodes",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }
  }

  if (showCleanUpDialog) {
    CleanUpDialog(
      totalSizeBytes = uiState.totalSizeBytes,
      episodes = uiState.episodes,
      onDismiss = { showCleanUpDialog = false },
      onConfirmCleanUp = { targetBytes ->
        viewModel.cleanUpDownloads(targetBytes)
        showCleanUpDialog = false
      }
    )
  }
}

@Composable
fun CleanUpDialog(
  totalSizeBytes: Long,
  episodes: List<DownloadedEpisodeItem>,
  onDismiss: () -> Unit,
  onConfirmCleanUp: (Long) -> Unit
) {
  var sliderPosition by remember { mutableFloatStateOf(0f) }

  val sortedOldestFirst = remember(episodes) {
    episodes.sortedWith(
      compareBy<DownloadedEpisodeItem> { it.downloadTime?.time ?: it.lastModified }
        .thenBy { it.lastModified }
    )
  }

  val (freedBytes, episodesToDeleteCount) = remember(sliderPosition, sortedOldestFirst) {
    val target = sliderPosition.toLong()
    var accum = 0L
    var count = 0
    for (ep in sortedOldestFirst) {
      if (accum + ep.sizeBytes <= target) {
        accum += ep.sizeBytes
        count++
      } else {
        break
      }
    }
    Pair(accum, count)
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Clean Up Downloads") },
    text = {
      Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Text(
          text = "Choose how much space to free up. The oldest downloaded episodes will be deleted first.",
          style = MaterialTheme.typography.bodyMedium
        )

        Slider(
          value = sliderPosition,
          onValueChange = { sliderPosition = it },
          valueRange = 0f..totalSizeBytes.toFloat().coerceAtLeast(1f)
        )

        OutlinedTextField(
          value = "${formatBytes(freedBytes)} ($episodesToDeleteCount episode${if (episodesToDeleteCount == 1) "" else "s"})",
          onValueChange = {},
          readOnly = true,
          label = { Text("Space to free") },
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = { onConfirmCleanUp(sliderPosition.toLong()) },
        enabled = freedBytes > 0 || sliderPosition > 0
      ) {
        Text("Clean Up")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}
