package com.podcreep.mobile.ui.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.podcreep.mobile.R
import com.podcreep.mobile.util.Server
import com.podcreep.mobile.util.humanizeDay

@Composable
fun SubscribedPodcasts(
  onPodcastDetailsClick: (podcastID: Long) -> Unit,
  viewModel : SubscribedPodcastsViewModel = hiltViewModel()) {

  val subscriptions = viewModel.subscriptions.collectAsState(initial = emptyList())

  LazyColumn {
    itemsIndexed(subscriptions.value) { index, sub ->
      val podcast = sub.podcast ?: return@itemsIndexed
      Row (
        modifier = Modifier.clickable {
          onPodcastDetailsClick(podcast.id)
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
            text = podcast.title,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontWeight = FontWeight.Bold
          )
          Text (
            text = AnnotatedString.fromHtml(podcast.description),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.alpha(0.6f),
          )
        }
      }
    }
  }
}
