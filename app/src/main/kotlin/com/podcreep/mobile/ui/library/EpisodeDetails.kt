package com.podcreep.mobile.ui.library

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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

      Column(modifier = Modifier.padding(vertical = 10.dp)) {
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
        },
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
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
