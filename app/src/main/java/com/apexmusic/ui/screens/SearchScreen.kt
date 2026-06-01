package com.apexmusic.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.*
import com.apexmusic.data.db.DownloadedSong
import com.apexmusic.ui.components.SearchSongItem
import com.apexmusic.ui.theme.*
import com.apexmusic.viewmodel.DownloadState
import com.apexmusic.viewmodel.MusicViewModel
import com.apexmusic.viewmodel.NowPlayingInfo
import com.apexmusic.viewmodel.SearchState

@Composable
fun SearchScreen(
    viewModel: MusicViewModel,
    onNavigateToPlayer: () -> Unit
) {
    val listState = rememberScalingLazyListState()
    val searchState by viewModel.searchState.collectAsState()
    val query by viewModel.searchQuery.collectAsState()
    val nowPlaying by viewModel.nowPlaying.collectAsState()
    val downloadStates by viewModel.downloadState.collectAsState()
    val downloadedSongs by viewModel.downloadedSongs.collectAsState(initial = emptyList())

    val focusRequester = remember { FocusRequester() }
    var localQuery by remember { mutableStateOf(query) }

    LaunchedEffect(Unit) {
        try { focusRequester.requestFocus() } catch (_: Exception) {}
    }

    ScreenScaffold(scrollState = listState) { contentPadding ->
        ScalingLazyColumn(
            state = listState,
            contentPadding = contentPadding,
            modifier = Modifier
                .fillMaxSize()
                .background(ApexBlack),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Search input box
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .background(ApexCardBg, androidx.compose.foundation.shape.RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Search,
                            contentDescription = null,
                            tint = ApexGrey,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        BasicTextField(
                            value = localQuery,
                            onValueChange = { localQuery = it },
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(focusRequester),
                            textStyle = TextStyle(
                                color = ApexWhite,
                                fontSize = 13.sp
                            ),
                            cursorBrush = SolidColor(ApexAccent),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(
                                onSearch = {
                                    if (localQuery.isNotBlank()) {
                                        viewModel.search(localQuery)
                                    }
                                }
                            ),
                            decorationBox = { innerTextField ->
                                if (localQuery.isEmpty()) {
                                    Text(
                                        "Search songs...",
                                        color = ApexGrey,
                                        fontSize = 13.sp
                                    )
                                }
                                innerTextField()
                            }
                        )
                    }
                }
            }

            // Search button
            item {
                Button(
                    onClick = {
                        if (localQuery.isNotBlank()) viewModel.search(localQuery)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ApexAccent)
                ) {
                    Text("Search", color = ApexBlack, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Results / states
            when (val state = searchState) {

                is SearchState.Loading -> {
                    item {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                progress = { -1f }
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Searching...", color = ApexGrey, fontSize = 12.sp)
                        }
                    }
                }

                is SearchState.Error -> {
                    item {
                        Text(
                            text = state.message,
                            color = ApexError,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                is SearchState.Success -> {
                    item {
                        Text(
                            text = "${state.results.size} results",
                            color = ApexGrey,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }

                    items(state.results) { item ->
                        val dlState = downloadStates[item.videoId] ?: DownloadState.Idle
                        val isDownloaded = downloadedSongs.any { it.videoId == item.videoId }

                        SearchSongItem(
                            item = item,
                            isCurrentSong = nowPlaying.videoId == item.videoId,
                            isDownloaded = isDownloaded || dlState is DownloadState.Success,
                            isDownloading = dlState is DownloadState.Downloading,
                            onPlay = {
                                viewModel.playSong(item)
                                onNavigateToPlayer()
                            },
                            onDownload = { viewModel.downloadSong(item) }
                        )
                    }
                }

                is SearchState.Idle -> {
                    if (query.isNotEmpty()) {
                        item {
                            Text(
                                text = "Tap Search to find songs",
                                color = ApexGrey,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}
