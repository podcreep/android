package com.podcreep.mobile.ui.library

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.podcreep.mobile.R
import com.podcreep.mobile.data.local.Episode
import com.podcreep.mobile.data.local.Podcast
import com.podcreep.mobile.util.Server

@Composable
fun EpisodeListEntry(podcast: Podcast, episode: Episode) {
  Row {
    AsyncImage(
      model = Server.url(podcast.imageUrl),
      placeholder = painterResource(R.drawable.ic_podcast),
      contentDescription = null,
      modifier = Modifier.size(100.dp)
    )
    Column(modifier = Modifier.padding(10.dp)) {
      Text(
        text = episode.title,
        maxLines = 2)
      Text (
        text = podcast.title,
        maxLines = 1,
        modifier = Modifier.alpha(0.6f))
    }
  }
}
