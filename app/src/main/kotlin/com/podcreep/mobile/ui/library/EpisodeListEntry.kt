package com.podcreep.mobile.ui.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.podcreep.mobile.R
import com.podcreep.mobile.data.local.Episode
import com.podcreep.mobile.data.local.Podcast
import com.podcreep.mobile.util.Server
import com.podcreep.mobile.util.formatSeconds
import com.podcreep.mobile.util.humanizeDay

@Composable
fun EpisodeListEntry(
  podcast: Podcast,
  episode: Episode,
  onEpisodeDetailsClick: (podcastID: Long, episodeID: Long) -> Unit,
  showProgressDuration: Boolean = true,
) {
  Row (
    modifier = Modifier
      .fillMaxWidth()
      .clickable {
        onEpisodeDetailsClick(podcast.id, episode.id)
      },
    verticalAlignment = Alignment.CenterVertically,
  ) {
    AsyncImage(
      model = Server.url(podcast.imageUrl),
      placeholder = painterResource(R.drawable.ic_podcast),
      contentDescription = null,
      modifier = Modifier.size(80.dp).padding(10.dp)
    )

    Column(
      modifier = Modifier
        .weight(1f)
        .padding(top = 5.dp, bottom = 5.dp)
    ) {
      Text(
        text = episode.pubDate.humanizeDay(context = LocalContext.current),
        maxLines = 1,
        modifier = Modifier.alpha(0.6f),
      )
      Text(
        text = episode.title,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
      Text (
        text = podcast.title,
        maxLines = 1,
        modifier = Modifier.alpha(0.6f),
      )
    }

    if (showProgressDuration) {
      Column(
        modifier = Modifier.padding(end = 16.dp, start = 8.dp),
        horizontalAlignment = Alignment.End,
      ) {
        Text(
          text = formatSeconds(episode.position),
          maxLines = 1,
          style = MaterialTheme.typography.bodyMedium,
          textAlign = TextAlign.End,
        )
        Text(
          text = formatSeconds(episode.durationSecs),
          maxLines = 1,
          modifier = Modifier.alpha(0.6f),
          style = MaterialTheme.typography.bodySmall,
          textAlign = TextAlign.End,
        )
      }
    }
  }
}
