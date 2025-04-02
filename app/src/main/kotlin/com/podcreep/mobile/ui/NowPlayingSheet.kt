package com.podcreep.mobile.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.podcreep.mobile.R
import com.podcreep.mobile.ui.views.AnimatedPlayPauseButton
import com.podcreep.mobile.util.Server

@Composable
fun NowPlayingView(viewModel: NowPlayingSheetViewModel = hiltViewModel()) {
    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val nowPlaying by viewModel.nowPlaying.collectAsStateWithLifecycle(viewModel.initialNowPlaying)

            AsyncImage(
                model = Server.url(nowPlaying.imageUrl),
                placeholder = painterResource(R.drawable.ic_podcast),
                contentDescription = null,
                modifier = Modifier.size(80.dp).padding(10.dp)
            )

            Text(nowPlaying.title, modifier = Modifier.weight(1f))

            when (nowPlaying.playState) {
                NowPlayingSheetViewModel.PlayState.STOPPED -> {
                    // Nothing
                }
                NowPlayingSheetViewModel.PlayState.BUFFERING -> {
                    CircularProgressIndicator(
                        modifier = Modifier.width(32.dp),
                        color = MaterialTheme.colorScheme.secondary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
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
    }
}
