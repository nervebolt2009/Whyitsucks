package com.apexmusic.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloaded_songs")
data class DownloadedSong(
    @PrimaryKey val videoId: String,
    val title: String,
    val artist: String,
    val thumbnailUrl: String,
    val localFilePath: String,       // absolute path to downloaded .m4a file
    val durationMs: Long = 0L,
    val downloadedAt: Long = System.currentTimeMillis(),
    val playlistName: String = "Downloads",
    val fileSizeBytes: Long = 0L
)

@Entity(tableName = "playlists")
data class Playlist(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "playlist_songs", primaryKeys = ["playlistId", "videoId"])
data class PlaylistSong(
    val playlistId: Long,
    val videoId: String,
    val addedAt: Long = System.currentTimeMillis(),
    val position: Int = 0
)

@Entity(tableName = "recent_plays")
data class RecentPlay(
    @PrimaryKey val videoId: String,
    val title: String,
    val artist: String,
    val thumbnailUrl: String,
    val playedAt: Long = System.currentTimeMillis()
)
