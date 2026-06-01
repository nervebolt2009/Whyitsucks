package com.apexmusic.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MusicDao {

    // ── Downloaded Songs ──────────────────────────────────────────────────────

    @Query("SELECT * FROM downloaded_songs ORDER BY downloadedAt DESC")
    fun getDownloadedSongs(): Flow<List<DownloadedSong>>

    @Query("SELECT * FROM downloaded_songs WHERE videoId = :videoId LIMIT 1")
    suspend fun getDownloadedSong(videoId: String): DownloadedSong?

    @Query("SELECT EXISTS(SELECT 1 FROM downloaded_songs WHERE videoId = :videoId)")
    fun isDownloaded(videoId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownloadedSong(song: DownloadedSong)

    @Delete
    suspend fun deleteDownloadedSong(song: DownloadedSong)

    @Query("DELETE FROM downloaded_songs WHERE videoId = :videoId")
    suspend fun deleteDownloadedSongById(videoId: String)

    // ── Playlists ─────────────────────────────────────────────────────────────

    @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
    fun getPlaylists(): Flow<List<Playlist>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: Playlist): Long

    @Delete
    suspend fun deletePlaylist(playlist: Playlist)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addSongToPlaylist(playlistSong: PlaylistSong)

    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId AND videoId = :videoId")
    suspend fun removeSongFromPlaylist(playlistId: Long, videoId: String)

    // ── Recent Plays ──────────────────────────────────────────────────────────

    @Query("SELECT * FROM recent_plays ORDER BY playedAt DESC LIMIT 20")
    fun getRecentPlays(): Flow<List<RecentPlay>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecentPlay(recentPlay: RecentPlay)

    @Query("DELETE FROM recent_plays WHERE videoId NOT IN (SELECT videoId FROM recent_plays ORDER BY playedAt DESC LIMIT 20)")
    suspend fun trimRecentPlays()
}
