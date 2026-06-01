package com.apexmusic.data.api

import com.google.gson.annotations.SerializedName

// ── Search Response ───────────────────────────────────────────────────────────

data class SearchResponse(
    @SerializedName("items") val items: List<SearchItem> = emptyList(),
    @SerializedName("nextpage") val nextPage: String? = null
)

data class SearchItem(
    @SerializedName("url") val url: String = "",          // e.g. "/watch?v=dQw4w9WgXcQ"
    @SerializedName("type") val type: String = "",
    @SerializedName("title") val title: String = "",
    @SerializedName("thumbnail") val thumbnail: String = "",
    @SerializedName("uploaderName") val uploaderName: String = "",
    @SerializedName("uploaderUrl") val uploaderUrl: String = "",
    @SerializedName("duration") val duration: Long = 0L,
    @SerializedName("views") val views: Long = 0L,
    @SerializedName("uploaded") val uploaded: Long = 0L
) {
    // Extract video ID from "/watch?v=VIDEO_ID"
    val videoId: String
        get() = url.substringAfter("v=").substringBefore("&").trim()
}

// ── Streams Response ──────────────────────────────────────────────────────────

data class StreamsResponse(
    @SerializedName("title") val title: String = "",
    @SerializedName("description") val description: String = "",
    @SerializedName("uploader") val uploader: String = "",
    @SerializedName("uploaderUrl") val uploaderUrl: String = "",
    @SerializedName("uploaderAvatar") val uploaderAvatar: String? = null,
    @SerializedName("thumbnailUrl") val thumbnailUrl: String = "",
    @SerializedName("duration") val duration: Long = 0L,
    @SerializedName("audioStreams") val audioStreams: List<AudioStream> = emptyList(),
    @SerializedName("dash") val dash: String? = null
) {
    /**
     * Pick the best audio stream for Wear OS:
     * - Prefer M4A (AAC) over WebM/Opus — more efficient hardware decoding on watch
     * - Target ~128kbps for best battery vs quality balance
     * - Fall back to any available stream
     */
    val bestAudioUrl: String?
        get() {
            val nonVideoStreams = audioStreams.filter { !it.videoOnly }
            if (nonVideoStreams.isEmpty()) return null

            // Try M4A ~128kbps first (itag 140, AAC hardware-decoded = best battery)
            val m4aStreams = nonVideoStreams.filter {
                it.mimeType.contains("audio/mp4") || it.format == "M4A"
            }
            if (m4aStreams.isNotEmpty()) {
                return m4aStreams
                    .filter { it.bitrate in 100_000..160_000 }
                    .minByOrNull { it.bitrate }?.url
                    ?: m4aStreams.minByOrNull { it.bitrate }?.url
            }

            // Fallback to any stream with lowest bitrate
            return nonVideoStreams.minByOrNull { it.bitrate }?.url
        }
}

data class AudioStream(
    @SerializedName("url") val url: String = "",
    @SerializedName("format") val format: String = "",
    @SerializedName("quality") val quality: String = "",
    @SerializedName("mimeType") val mimeType: String = "",
    @SerializedName("codec") val codec: String = "",
    @SerializedName("bitrate") val bitrate: Int = 0,
    @SerializedName("videoOnly") val videoOnly: Boolean = false,
    @SerializedName("itag") val itag: Int = 0,
    @SerializedName("contentLength") val contentLength: Long = 0L
)
