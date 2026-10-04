package com.podcreep.mobile.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.podcreep.mobile.R
import com.podcreep.mobile.ui.views.AnimatedPlayPauseButton
import com.podcreep.mobile.util.Server
import java.util.Locale

private fun formatTime(ms: Long): String {
  val totalSeconds = (ms.coerceAtLeast(0) / 1000).toInt()
  val hours = totalSeconds / 3600
  val minutes = (totalSeconds % 3600) / 60
  val seconds = totalSeconds % 60
  return if (hours > 0) {
    String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds)
  } else {
    String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
  }
}

@Composable
fun NowPlayingView(
    modifier: Modifier = Modifier,
    viewModel: NowPlayingSheetViewModel = hiltViewModel()
) {
  val nowPlaying by viewModel.nowPlaying.collectAsStateWithLifecycle(
      viewModel.initialNowPlaying)

  Column(modifier.fillMaxSize()) {
    Row(
      modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      AsyncImage(
          model = nowPlaying.imageUrl.apply { Server.url(this) },
          placeholder = painterResource(R.drawable.ic_podcast),
          contentDescription = null,
          modifier = Modifier.size(80.dp).padding(10.dp))

      Text(
        nowPlaying.title,
        modifier = Modifier.weight(1f)
          .padding(end = 10.dp))

      when (nowPlaying.playState) {
        NowPlayingSheetViewModel.PlayState.STOPPED -> {
          // Nothing
        }
        NowPlayingSheetViewModel.PlayState.BUFFERING -> {
          CircularProgressIndicator(
              modifier = Modifier.width(32.dp),
              color = MaterialTheme.colorScheme.secondary,
              trackColor = MaterialTheme.colorScheme.surfaceVariant)
        }
        else -> {
          AnimatedPlayPauseButton(
              onPlayClick = { viewModel.play() },
              onPauseClick = { viewModel.pause() },
              playing = nowPlaying.playState == NowPlayingSheetViewModel.PlayState.PLAYING,
          )
        }
      }
    }

    Row(
      modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 4.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      val progress = if (nowPlaying.durationMs > 0) {
        (nowPlaying.positionMs.toFloat() / nowPlaying.durationMs.toFloat()).coerceIn(0f, 1f)
      } else {
        0f
      }

      LinearProgressIndicator(
        progress = { progress },
        modifier = Modifier
          .weight(1f)
          .padding(end = 12.dp)
      )

      Text(
        text = "${formatTime(nowPlaying.positionMs)} / ${formatTime(nowPlaying.durationMs)}",
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.padding(end = 8.dp)
      )

      IconButton(
        onClick = { viewModel.skipBack() },
        modifier = Modifier.size(36.dp)
      ) {
        Icon(
          painter = painterResource(R.drawable.ic_rewind_10_24dp),
          contentDescription = stringResource(R.string.contentdesc_rewind_10)
        )
      }

      IconButton(
        onClick = { viewModel.skipForward() },
        modifier = Modifier.size(36.dp)
      ) {
        Icon(
          painter = painterResource(R.drawable.ic_forward_30_24dp),
          contentDescription = stringResource(R.string.contentdesc_forward_30)
        )
      }
    }
    Text (
      text = AnnotatedString.fromHtml(nowPlaying.description),
      modifier = Modifier
        .padding(horizontal = 16.dp, vertical = 8.dp)
        .verticalScroll(rememberScrollState())
    )
  }
}
