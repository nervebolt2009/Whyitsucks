package com.apexmusic.ui.screens
import androidx.compose.material.icons.Icons

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.*
import com.apexmusic.ui.components.LibrarySongItem
import com.apexmusic.ui.theme.*
import com.apexmusic.viewmodel.MusicViewModel

@Composable
fun LibraryScreen(
    viewModel: MusicViewModel,
    onNavigateToPlayer: () -> Unit
) {
    val listState = rememberScalingLazyListState()
    val downloadedSongs by viewModel.downloadedSongs.collectAsState(initial = emptyList())
    val nowPlaying by viewModel.nowPlaying.collectAsState()

    ScreenScaffold(scrollState = listState) { contentPadding ->
        ScalingLazyColumn(
            state = listState,
            contentPadding = contentPadding,
            modifier = Modifier
                .fillMaxSize()
                .background(ApexBlack),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            item {
                Text(
                    text = "Downloads",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = ApexWhite,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            if (downloadedSongs.isEmpty()) {
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Download,
                            contentDescription = null,
                            tint = ApexGrey,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "No downloads yet",
                            color = ApexGrey,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Tap ↓ on any song",
                            color = ApexDivider,
                            fontSize = 11.sp
                        )
                    }
                }
            } else {
                item {
                    Text(
                        text = "${downloadedSongs.size} songs",
                        color = ApexGrey,
                        fontSize = 10.sp
                    )
                }

                items(downloadedSongs) { song ->
                    LibrarySongItem(
                        song = song,
                        isCurrentSong = nowPlaying.videoId == song.videoId,
                        onPlay = {
                            viewModel.playDownloadedSong(song)
                            onNavigateToPlayer()
                        },
                        onDelete = { viewModel.deleteDownload(song) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}
