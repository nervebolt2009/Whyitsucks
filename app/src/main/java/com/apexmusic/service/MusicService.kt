package com.apexmusic.service

import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

/**
 * Background music playback service.
 *
 * Key battery optimizations for GW7 (425mAh battery):
 * 1. Audio Offload — offloads audio decoding to dedicated DSP chip,
 *    CPU sleeps while music plays → huge battery saving
 * 2. USAGE_MEDIA + setHandleAudioBecomingNoisy — proper audio focus management
 * 3. MediaSessionService handles foreground notification automatically
 */
class MusicService : MediaSessionService() {

    private lateinit var player: ExoPlayer
    private lateinit var mediaSession: MediaSession

    override fun onCreate() {
        super.onCreate()

        player = ExoPlayer.Builder(this)
            // Proper audio attributes for music streaming
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                /* handleAudioFocus= */ true
            )
            // Automatically pause when headphones disconnected
            .setHandleAudioBecomingNoisy(true)
            .build()
            .also { exoPlayer ->
                // ── AUDIO OFFLOAD: biggest battery win on Wear OS ──────────────
                // Allows audio to be processed by dedicated hardware DSP,
                // CPU can sleep → extends battery life significantly
                exoPlayer.experimentalSetOffloadSchedulingEnabled(true)

                exoPlayer.trackSelectionParameters =
                    exoPlayer.trackSelectionParameters
                        .buildUpon()
                        .setAudioOffloadPreferences(
                            TrackSelectionParameters.AudioOffloadPreferences.Builder()
                                .setAudioOffloadMode(
                                    TrackSelectionParameters.AudioOffloadPreferences
                                        .AUDIO_OFFLOAD_MODE_ENABLED
                                )
                                .setIsGaplessSupportRequired(false)
                                .setIsSpeedChangeSupportRequired(false)
                                .build()
                        )
                        .build()
            }

        // MediaSession bridges the player to Wear OS media controls,
        // notification controls, and other apps requesting media info
        mediaSession = MediaSession.Builder(this, player).build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession =
        mediaSession

    override fun onDestroy() {
        mediaSession.release()
        player.release()
        super.onDestroy()
    }
}
