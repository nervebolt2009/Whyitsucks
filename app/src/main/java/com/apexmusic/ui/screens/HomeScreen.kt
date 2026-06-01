package com.apexmusic.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.*
import coil.compose.AsyncImage
import com.apexmusic.ui.theme.*
import com.apexmusic.viewmodel.MusicViewModel

@Composable
fun HomeScreen(
    viewModel: MusicViewModel,
    onNavigateToSearch: () -> Unit,
    onNavigateToPlayer: () -> Unit,
    onNavigateToLibrary: () -> Unit
) {
    val listState = rememberScalingLazyListState()
    val recentPlays by viewModel.recentPlays.collectAsState(initial = emptyList())
    val nowPlaying by viewModel.nowPlaying.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()

    ScreenScaffold(scrollState = listState) { contentPadding ->
        ScalingLazyColumn(
            state = listState,
            contentPadding = contentPadding,
            modifier = Modifier
                .fillMaxSize()
                .background(ApexBlack),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // App title
            item {
                Text(
                    text = "ApexMusic",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = ApexAccent,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            // Search button
            item {
                Chip(
                    onClick = onNavigateToSearch,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    label = { Text("Search", color = ApexWhite, fontSize = 14.sp) },
                    icon = {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Search,
                            contentDescription = null,
                            tint = ApexAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    colors = ChipDefaults.chipColors(containerColor = ApexCardBg)
                )
            }

            // Library button
            item {
                Chip(
                    onClick = onNavigateToLibrary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    label = { Text("Downloads", color = ApexWhite, fontSize = 14.sp) },
                    icon = {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.LibraryMusic,
                            contentDescription = null,
                            tint = ApexAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    colors = ChipDefaults.chipColors(containerColor = ApexCardBg)
                )
            }

            // Now Playing mini-bar (if something is playing)
            if (nowPlaying.title.isNotEmpty()) {
                item {
                    Chip(
                        onClick = onNavigateToPlayer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        label = {
                            Text(
                                text = nowPlaying.title,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = ApexWhite,
                                fontSize = 12.sp
                            )
                        },
                        secondaryLabel = {
                            Text(
                                text = if (isPlaying) "▶ Playing" else "⏸ Paused",
                                fontSize = 10.sp,
                                color = if (isPlaying) ApexAccent else ApexGrey
                            )
                        },
                        icon = {
                            AsyncImage(
                                model = nowPlaying.thumbnailUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                contentScale = ContentScale.Crop
                            )
                        },
                        colors = ChipDefaults.chipColors(
                            containerColor = ApexAccent.copy(alpha = 0.15f)
                        )
                    )
                }
            }

            // Recent plays section
            if (recentPlays.isNotEmpty()) {
                item {
                    Text(
                        text = "Recent",
                        fontSize = 11.sp,
                        color = ApexGrey,
                        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                    )
                }

                items(recentPlays.take(5)) { recent ->
                    Chip(
                        onClick = {
                            // Re-play recent song
                            viewModel.search(recent.title)
                            onNavigateToSearch()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        label = {
                            Text(
                                text = recent.title,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontSize = 12.sp,
                                color = ApexWhite
                            )
                        },
                        secondaryLabel = {
                            Text(
                                text = recent.artist,
                                fontSize = 10.sp,
                                color = ApexGrey
                            )
                        },
                        icon = {
                            AsyncImage(
                                model = recent.thumbnailUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                contentScale = ContentScale.Crop
                            )
                        },
                        colors = ChipDefaults.chipColors(containerColor = ApexCardBg)
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}
