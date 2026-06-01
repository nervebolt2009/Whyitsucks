package com.apexmusic.ui.components
import androidx.wear.compose.material.TwoSlotChip
import androidx.wear.compose.material.ChipDefaults
import androidx.compose.material.icons.Icons

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.*
import coil.compose.AsyncImage
import com.apexmusic.data.api.SearchItem
import com.apexmusic.data.db.DownloadedSong
import com.apexmusic.ui.theme.*

/**
 * Song row for search results — optimized for GW7 480x480 round display
 */
@Composable
fun SearchSongItem(
    item: SearchItem,
    isCurrentSong: Boolean,
    isDownloaded: Boolean,
    isDownloading: Boolean,
    onPlay: () -> Unit,
    onDownload: () -> Unit,
    modifier: Modifier = Modifier
) {
    TwoSlotChip(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        onClick = onPlay,
        label = {
            Text(
                text = item.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 13.sp,
                fontWeight = if (isCurrentSong) FontWeight.Bold else FontWeight.Normal,
                color = if (isCurrentSong) ApexAccent else ApexWhite
            )
        },
        secondaryLabel = {
            Text(
                text = item.uploaderName,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 11.sp,
                color = ApexGrey
            )
        },
        icon = {
            AsyncImage(
                model = item.thumbnail,
                contentDescription = null,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(6.dp)),
                contentScale = ContentScale.Crop
            )
        },
        secondaryIcon = {
            if (isDownloading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    progress = { -1f }  // indeterminate
                )
            } else if (isDownloaded) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.DownloadDone,
                    contentDescription = "Downloaded",
                    tint = ApexAccent,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                IconButton(
                    onClick = onDownload,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Download,
                        contentDescription = "Download",
                        tint = ApexGrey,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
        colors = ChipDefaults.chipColors(
            containerColor = if (isCurrentSong) ApexAccent.copy(alpha = 0.12f) else ApexCardBg
        )
    )
}

/**
 * Song row for library / downloaded songs
 */
@Composable
fun LibrarySongItem(
    song: DownloadedSong,
    isCurrentSong: Boolean,
    onPlay: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    TwoSlotChip(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        onClick = onPlay,
        label = {
            Text(
                text = song.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 13.sp,
                fontWeight = if (isCurrentSong) FontWeight.Bold else FontWeight.Normal,
                color = if (isCurrentSong) ApexAccent else ApexWhite
            )
        },
        secondaryLabel = {
            Text(
                text = song.artist,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 11.sp,
                color = ApexGrey
            )
        },
        icon = {
            AsyncImage(
                model = song.thumbnailUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(6.dp)),
                contentScale = ContentScale.Crop
            )
        },
        secondaryIcon = {
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = ApexError,
                    modifier = Modifier.size(18.dp)
                )
            }
        },
        colors = ChipDefaults.chipColors(
            containerColor = if (isCurrentSong) ApexAccent.copy(alpha = 0.12f) else ApexCardBg
        )
    )
}

/** Duration Long → "m:ss" string */
fun Long.toMinuteSecond(): String {
    val totalSeconds = this / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "$minutes:${seconds.toString().padStart(2, '0')}"
}
