package com.apexmusic.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.*
import coil.compose.AsyncImage
import com.apexmusic.ui.components.toMinuteSecond
import com.apexmusic.ui.theme.*
import com.apexmusic.viewmodel.MusicViewModel

@Composable
fun PlayerScreen(viewModel: MusicViewModel) {

    val nowPlaying by viewModel.nowPlaying.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val progress by viewModel.progress.collectAsState()
    val durationMs by viewModel.durationMs.collectAsState()
    val isBuffering by viewModel.isBuffering.collectAsState()
    val playError by viewModel.playError.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ApexBlack),
        contentAlignment = Alignment.Center
    ) {
        if (nowPlaying.title.isEmpty()) {
            // Nothing playing state
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = ApexGrey,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Nothing playing",
                    color = ApexGrey,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Search to start",
                    color = ApexDivider,
                    fontSize = 11.sp
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                // Album artwork
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(ApexCardBg),
                    contentAlignment = Alignment.Center
                ) {
                    if (nowPlaying.thumbnailUrl.isNotEmpty()) {
                        AsyncImage(
                            model = nowPlaying.thumbnailUrl,
                            contentDescription = "Album art",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = ApexAccent,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    // Buffering overlay
                    if (isBuffering) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.6f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                progress = { -1f },
                                strokeWidth = 2.dp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Song title
                Text(
                    text = nowPlaying.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = ApexWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                // Artist
                Text(
                    text = nowPlaying.artist,
                    fontSize = 11.sp,
                    color = ApexGrey,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )

                // Error message
                if (playError != null) {
                    Text(
                        text = playError!!,
                        fontSize = 10.sp,
                        color = ApexError,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Progress bar
                Column(modifier = Modifier.fillMaxWidth()) {
                    LinearProgressIndicator(
                        progress = { progress.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = ApexAccent,
                        trackColor = ApexDivider
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = (progress * durationMs).toLong().toMinuteSecond(),
                            fontSize = 9.sp,
                            color = ApexGrey
                        )
                        Text(
                            text = durationMs.toMinuteSecond(),
                            fontSize = 9.sp,
                            color = ApexGrey
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Playback controls — skip | play/pause | skip
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Previous
                    IconButton(
                        onClick = { viewModel.skipPrevious() },
                        modifier = Modifier
                            .size(36.dp)
                            .background(ApexCardBg, CircleShape)
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.SkipPrevious,
                            contentDescription = "Previous",
                            tint = ApexWhite,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Play / Pause — main button, larger
                    IconButton(
                        onClick = { viewModel.togglePlayPause() },
                        modifier = Modifier
                            .size(48.dp)
                            .background(ApexAccent, CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) {
                                androidx.compose.material.icons.Icons.Default.Pause
                            } else {
                                androidx.compose.material.icons.Icons.Default.PlayArrow
                            },
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = ApexBlack,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Next
                    IconButton(
                        onClick = { viewModel.skipNext() },
                        modifier = Modifier
                            .size(36.dp)
                            .background(ApexCardBg, CircleShape)
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.SkipNext,
                            contentDescription = "Next",
                            tint = ApexWhite,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
