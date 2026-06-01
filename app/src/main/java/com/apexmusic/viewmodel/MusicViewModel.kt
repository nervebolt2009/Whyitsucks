package com.apexmusic.viewmodel

import android.app.Application
import android.content.ComponentName
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.apexmusic.ApexMusicApp
import com.apexmusic.data.api.SearchItem
import com.apexmusic.data.db.DownloadedSong
import com.apexmusic.service.MusicService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NowPlayingInfo(
    val videoId: String = "",
    val title: String = "",
    val artist: String = "",
    val thumbnailUrl: String = ""
)

sealed class SearchState {
    object Idle : SearchState()
    object Loading : SearchState()
    data class Success(val results: List<SearchItem>) : SearchState()
    data class Error(val message: String) : SearchState()
}

sealed class DownloadState {
    object Idle : DownloadState()
    object Downloading : DownloadState()
    object Success : DownloadState()
    data class Error(val message: String) : DownloadState()
}

class MusicViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as ApexMusicApp).repository

    // ── Search State ──────────────────────────────────────────────────────────
    private val _searchState = MutableStateFlow<SearchState>(SearchState.Idle)
    val searchState: StateFlow<SearchState> = _searchState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // ── Playback State ────────────────────────────────────────────────────────
    private val _nowPlaying = MutableStateFlow(NowPlayingInfo())
    val nowPlaying: StateFlow<NowPlayingInfo> = _nowPlaying.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _progress = MutableStateFlow(0f)         // 0.0 → 1.0
    val progress: StateFlow<Float> = _progress.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _isBuffering = MutableStateFlow(false)
    val isBuffering: StateFlow<Boolean> = _isBuffering.asStateFlow()

    private val _playError = MutableStateFlow<String?>(null)
    val playError: StateFlow<String?> = _playError.asStateFlow()

    // ── Download State ────────────────────────────────────────────────────────
    private val _downloadState = MutableStateFlow<Map<String, DownloadState>>(emptyMap())
    val downloadState: StateFlow<Map<String, DownloadState>> = _downloadState.asStateFlow()

    // ── Library ───────────────────────────────────────────────────────────────
    val downloadedSongs = repository.getDownloadedSongs()
    val recentPlays = repository.getRecentPlays()

    // ── MediaController ───────────────────────────────────────────────────────
    private var mediaController: MediaController? = null

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlayingNow: Boolean) {
            _isPlaying.value = isPlayingNow
        }

        override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
            _nowPlaying.value = _nowPlaying.value.copy(
                title = mediaMetadata.title?.toString() ?: _nowPlaying.value.title,
                artist = mediaMetadata.artist?.toString() ?: _nowPlaying.value.artist,
                thumbnailUrl = mediaMetadata.artworkUri?.toString() ?: _nowPlaying.value.thumbnailUrl
            )
        }

        override fun onPlaybackStateChanged(state: Int) {
            _isBuffering.value = state == Player.STATE_BUFFERING
        }
    }

    init {
        connectToMediaService()
        startProgressPolling()
    }

    private fun connectToMediaService() {
        val sessionToken = SessionToken(
            getApplication(),
            ComponentName(getApplication(), MusicService::class.java)
        )
        val future = MediaController.Builder(getApplication(), sessionToken).buildAsync()
        future.addListener(
            {
                try {
                    mediaController = future.get()
                    mediaController?.addListener(playerListener)
                    // Sync initial state
                    _isPlaying.value = mediaController?.isPlaying ?: false
                } catch (_: Exception) { }
            },
            ContextCompat.getMainExecutor(getApplication())
        )
    }

    private fun startProgressPolling() {
        viewModelScope.launch {
            while (true) {
                mediaController?.let { ctrl ->
                    val dur = ctrl.duration
                    val pos = ctrl.currentPosition
                    if (dur > 0) {
                        _progress.value = pos.toFloat() / dur.toFloat()
                        _durationMs.value = dur
                    }
                }
                delay(500)
            }
        }
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun search(query: String) {
        if (query.isBlank()) return
        _searchQuery.value = query
        viewModelScope.launch {
            _searchState.value = SearchState.Loading
            try {
                val results = repository.search(query)
                _searchState.value = if (results.isEmpty()) {
                    SearchState.Error("No results found for \"$query\"")
                } else {
                    SearchState.Success(results)
                }
            } catch (e: Exception) {
                _searchState.value = SearchState.Error("Search failed. Check your connection.")
            }
        }
    }

    fun clearSearch() {
        _searchState.value = SearchState.Idle
        _searchQuery.value = ""
    }

    /**
     * Play a song from search results (streams via Piped/YouTube)
     */
    fun playSong(item: SearchItem) {
        viewModelScope.launch {
            _isBuffering.value = true
            _playError.value = null
            try {
                // Always fetch fresh stream URL — YouTube URLs expire
                val streamInfo = repository.getStreamInfo(item.videoId)
                val audioUrl = streamInfo.bestAudioUrl
                    ?: run {
                        _playError.value = "No audio stream available"
                        _isBuffering.value = false
                        return@launch
                    }

                val mediaItem = MediaItem.Builder()
                    .setUri(audioUrl)
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle(item.title)
                            .setArtist(item.uploaderName)
                            .setArtworkUri(Uri.parse(item.thumbnail))
                            .build()
                    )
                    .build()

                mediaController?.run {
                    setMediaItem(mediaItem)
                    prepare()
                    play()
                }

                _nowPlaying.value = NowPlayingInfo(
                    videoId = item.videoId,
                    title = item.title,
                    artist = item.uploaderName,
                    thumbnailUrl = item.thumbnail
                )

                // Log to recents
                repository.addRecentPlay(item.videoId, item.title, item.uploaderName, item.thumbnail)

            } catch (e: Exception) {
                _playError.value = "Playback failed. Try again."
                _isBuffering.value = false
            }
        }
    }

    /**
     * Play a downloaded (offline) song
     */
    fun playDownloadedSong(song: DownloadedSong) {
        val mediaItem = MediaItem.Builder()
            .setUri(Uri.fromFile(java.io.File(song.localFilePath)))
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(song.title)
                    .setArtist(song.artist)
                    .setArtworkUri(Uri.parse(song.thumbnailUrl))
                    .build()
            )
            .build()

        mediaController?.run {
            setMediaItem(mediaItem)
            prepare()
            play()
        }

        _nowPlaying.value = NowPlayingInfo(
            videoId = song.videoId,
            title = song.title,
            artist = song.artist,
            thumbnailUrl = song.thumbnailUrl
        )
    }

    fun togglePlayPause() {
        mediaController?.run {
            if (isPlaying) pause() else play()
        }
    }

    fun seekTo(fraction: Float) {
        val position = (fraction * _durationMs.value).toLong()
        mediaController?.seekTo(position)
    }

    fun skipNext() = mediaController?.seekToNext()
    fun skipPrevious() = mediaController?.seekToPrevious()

    fun downloadSong(item: SearchItem) {
        viewModelScope.launch {
            val currentStates = _downloadState.value.toMutableMap()
            currentStates[item.videoId] = DownloadState.Downloading
            _downloadState.value = currentStates

            val result = repository.downloadSong(item)

            val updated = _downloadState.value.toMutableMap()
            updated[item.videoId] = if (result.isSuccess) {
                DownloadState.Success
            } else {
                DownloadState.Error(result.exceptionOrNull()?.message ?: "Download failed")
            }
            _downloadState.value = updated
        }
    }

    fun deleteDownload(song: DownloadedSong) {
        viewModelScope.launch {
            repository.deleteDownload(song)
        }
    }

    override fun onCleared() {
        mediaController?.removeListener(playerListener)
        mediaController?.release()
        super.onCleared()
    }
}
