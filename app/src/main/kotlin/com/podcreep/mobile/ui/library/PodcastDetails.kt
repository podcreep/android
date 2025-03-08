package com.podcreep.mobile.ui.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.unit.dp
import androidx.core.text.HtmlCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.podcreep.mobile.R
import com.podcreep.mobile.util.Server

@Composable
fun PodcastDetails(onEpisodeDetailsClick: (podcastID: Long, episodeID: Long) -> Unit, viewModel: PodcastDetailsViewModel = hiltViewModel()) {
    val podcast = viewModel.podcast.collectAsState(initial = null).value ?: return
    val episodes = viewModel.episodes.collectAsState(initial = emptyList())
    var needExpandedDescription by remember { mutableStateOf(false) }
    var expandedDescription by remember { mutableStateOf(false) }

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
            }
        }
        Column (Modifier.clickable(enabled = needExpandedDescription) {
          expandedDescription = !expandedDescription
        }) {
            Text(
                text = AnnotatedString.fromHtml(podcast.description),
                onTextLayout = { layout ->
                    needExpandedDescription = expandedDescription || layout.hasVisualOverflow
                },
                maxLines = if (expandedDescription) Int.MAX_VALUE else 4,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            if (needExpandedDescription) {
                Text(
                    text = stringResource(if(expandedDescription) R.string.less else R.string.more),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
            }
        }
        LazyColumn {
            itemsIndexed(episodes.value) { index, episode ->
                EpisodeListEntry(podcast, episode, onEpisodeDetailsClick)
            }
        }
    }
}