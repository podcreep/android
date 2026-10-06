package com.podcreep.mobile.ui.library

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.podcreep.mobile.R
import com.podcreep.mobile.domain.cache.EpisodeMediaCache
import com.podcreep.mobile.util.Server

@Composable
fun EpisodeDetails(viewModel: EpisodeDetailsViewModel = hiltViewModel()) {
  val episode = viewModel.episode.collectAsState(initial = null).value
  val podcast = viewModel.podcast.collectAsState(initial = null).value
  val downloadProgress = viewModel.downloadProgress.collectAsState(
    initial = EpisodeMediaCache.DownloadProgress(EpisodeMediaCache.Status.NotDownloaded)
  ).value

  if (episode == null || podcast == null) {
    return
  }

  Column {
    Row {
      AsyncImage(
        model = Server.url(podcast.imageUrl),
        placeholder = painterResource(R.drawable.ic_podcast),
        contentDescription = null,
        modifier = Modifier.size(80.dp).padding(10.dp)
      )

      Column(modifier = Modifier.padding(vertical = 10.dp)) {
        Text(
          text = podcast.title
        )
        Text(
          text = episode.title
        )
      }
    }
    Row(
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(
        onClick = { viewModel.download() },
        enabled = downloadProgress.status == EpisodeMediaCache.Status.NotDownloaded ||
            downloadProgress.status == EpisodeMediaCache.Status.DownloadFailed
      ) {
        Icon(
          imageVector = ImageVector.vectorResource(
            if (downloadProgress.status == EpisodeMediaCache.Status.Downloaded)
              R.drawable.ic_done_24dp
            else
              R.drawable.ic_download_24dp
          ),
          modifier = Modifier.size(32.dp),
          contentDescription = stringResource(R.string.download)
        )
      }
      Spacer(Modifier.width(8.dp))
      Text(
        text = when (downloadProgress.status) {
          EpisodeMediaCache.Status.NotDownloaded -> stringResource(R.string.status_not_downloaded)
          EpisodeMediaCache.Status.InProgress -> {
            if (downloadProgress.progressPercent != null && downloadProgress.progressPercent >= 0) {
              "Downloading (${downloadProgress.progressPercent}%)"
            } else {
              stringResource(R.string.status_downloading)
            }
          }
          EpisodeMediaCache.Status.Downloaded -> stringResource(R.string.status_downloaded)
          EpisodeMediaCache.Status.DownloadFailed -> stringResource(R.string.status_download_failed)
        },
        style = MaterialTheme.typography.bodyMedium
      )
      Spacer(Modifier.weight(1f))
      Button(onClick = {
          viewModel.play()
        }) {
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.ic_play_arrow_black_24dp),
            modifier = Modifier.size(32.dp),
            contentDescription = stringResource(R.string.play)
        )
      }
    }
    Text (
      text = AnnotatedString.fromHtml(episode.description),
      modifier = Modifier
        .padding(horizontal = 16.dp, vertical = 8.dp)
        .verticalScroll(rememberScrollState())
    )
  }
}
