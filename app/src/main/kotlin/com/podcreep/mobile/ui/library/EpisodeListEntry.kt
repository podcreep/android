package com.podcreep.mobile.ui.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
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
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.podcreep.mobile.R
import com.podcreep.mobile.data.local.Episode
import com.podcreep.mobile.data.local.Podcast
import com.podcreep.mobile.util.Server
import com.podcreep.mobile.util.humanizeDay
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale

@Composable
fun EpisodeListEntry(
  podcast: Podcast,
  episode: Episode,
  onEpisodeDetailsClick: (podcastID: Long, episodeID: Long) -> Unit
) {
  Row (
    modifier = Modifier.clickable {
      onEpisodeDetailsClick(podcast.id, episode.id)
    }
  ) {
    AsyncImage(
      model = Server.url(podcast.imageUrl),
      placeholder = painterResource(R.drawable.ic_podcast),
      contentDescription = null,
      modifier = Modifier.size(80.dp).padding(10.dp)
    )



    
    Column(modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)) {
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
  }
}
