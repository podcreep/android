package com.podcreep.mobile.ui.library

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.podcreep.mobile.R
import com.podcreep.mobile.util.Server

@Composable
fun EpisodeDetails(viewModel: EpisodeDetailsViewModel = hiltViewModel()) {
  val episode = viewModel.episode.collectAsState(initial = null).value
  val podcast = viewModel.podcast.collectAsState(initial = null).value

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

      Column {
        Text(
          text = podcast.title
        )
        Text(
          text = episode.title
        )
      }
    }
    Row {
      Spacer(Modifier.weight(1f))
      Button(onClick = {
        viewModel.play()
        }) {
        Text(
          text = "Play"
        )
      }
    }
    Text (
      text = episode.description
    )
  }
}
