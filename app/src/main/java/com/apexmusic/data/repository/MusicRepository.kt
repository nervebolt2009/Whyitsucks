package com.apexmusic.data.repository

import android.content.Context
import android.os.Environment
import com.apexmusic.data.api.PipedApiClient
import com.apexmusic.data.api.SearchItem
import com.apexmusic.data.api.StreamsResponse
import com.apexmusic.data.db.AppDatabase
import com.apexmusic.data.db.DownloadedSong
import com.apexmusic.data.db.RecentPlay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit

class MusicRepository(
    private val context: Context,
    private val database: AppDatabase
) {

    private val api = PipedApiClient.service
    private val dao = database.musicDao()

    // ── Search ────────────────────────────────────────────────────────────────

    suspend fun search(query: String): List<SearchItem> = withContext(Dispatchers.IO) {
        val response = api.search(query, filter = "music_songs")
        response.items.filter { it.type == "stream" && it.videoId.isNotEmpty() }
    }

    // ── Stream Info ───────────────────────────────────────────────────────────

    /**
     * Fetches fresh stream URLs (YouTube signed URLs expire — always fetch fresh on play)
     */
    suspend fun getStreamInfo(videoId: String): StreamsResponse = withContext(Dispatchers.IO) {
        api.getStreams(videoId)
    }

    // ── Recent Plays ──────────────────────────────────────────────────────────

    fun getRecentPlays() = dao.getRecentPlays()

    suspend fun addRecentPlay(
        videoId: String, title: String, artist: String, thumbnailUrl: String
    ) = withContext(Dispatchers.IO) {
        dao.insertRecentPlay(RecentPlay(videoId, title, artist, thumbnailUrl))
        dao.trimRecentPlays()
    }

    // ── Downloads ─────────────────────────────────────────────────────────────

    fun getDownloadedSongs(): Flow<List<DownloadedSong>> = dao.getDownloadedSongs()

    fun isDownloaded(videoId: String) = dao.isDownloaded(videoId)

    suspend fun downloadSong(item: SearchItem): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // 1. Fetch fresh audio URL
            val streamInfo = api.getStreams(item.videoId)
            val audioUrl = streamInfo.bestAudioUrl
                ?: return@withContext Result.failure(Exception("No audio stream available"))

            // 2. Create download directory
            val downloadDir = File(
                context.getExternalFilesDir(Environment.DIRECTORY_MUSIC),
                "ApexMusic"
            ).also { it.mkdirs() }

            val safeTitle = item.title
                .replace(Regex("[^a-zA-Z0-9._-]"), "_")
                .take(50)
            val outFile = File(downloadDir, "${item.videoId}_$safeTitle.m4a")

            // 3. Download with OkHttp
            val client = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(300, TimeUnit.SECONDS)
                .build()

            val request = Request.Builder().url(audioUrl).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("Download failed: ${response.code}"))
                }
                response.body?.byteStream()?.use { input ->
                    outFile.outputStream().use { output ->
                        input.copyTo(output, bufferSize = 8192)
                    }
                }
            }

            // 4. Save to Room database
            dao.insertDownloadedSong(
                DownloadedSong(
                    videoId = item.videoId,
                    title = item.title,
                    artist = item.uploaderName,
                    thumbnailUrl = item.thumbnail,
                    localFilePath = outFile.absolutePath,
                    durationMs = item.duration * 1000,
                    fileSizeBytes = outFile.length()
                )
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteDownload(song: DownloadedSong) = withContext(Dispatchers.IO) {
        // Delete file from storage
        File(song.localFilePath).takeIf { it.exists() }?.delete()
        // Remove from DB
        dao.deleteDownloadedSong(song)
    }

    // ── Playlists ─────────────────────────────────────────────────────────────

    fun getPlaylists() = dao.getPlaylists()

    suspend fun createPlaylist(name: String): Long = withContext(Dispatchers.IO) {
        dao.insertPlaylist(com.apexmusic.data.db.Playlist(name = name))
    }
}
