package com.example.data.repository

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.os.Build
import android.util.Log
import com.example.data.local.entities.TrackEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import kotlin.random.Random

enum class RepeatMode {
    OFF,
    ALL,
    ONE
}

data class PlayerPlaybackState(
    val currentTrack: TrackEntity? = null,
    val isPlaying: Boolean = false,
    val currentPositionSec: Int = 0,
    val durationSec: Int = 0,
    val queue: List<TrackEntity> = emptyList(),
    val isShuffle: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val playbackSpeed: Float = 1.0f,
    val audioQuality: String = "320 kbps (High)",
    val waveformLevels: List<Float> = List(24) { 0.2f },
    val isBuffering: Boolean = false
)

class AudioPlayerEngine(
    private val context: Context? = null,
    private val onTrackPlayed: (track: TrackEntity, duration: Int) -> Unit
) {
    private val TAG = "AudioPlayerEngine"
    private val engineScope = CoroutineScope(Dispatchers.Main)
    private var progressJob: Job? = null

    private val _playbackState = MutableStateFlow(PlayerPlaybackState())
    val playbackState: StateFlow<PlayerPlaybackState> = _playbackState.asStateFlow()

    // Dual-player setup for instant gapless switching
    private var activePlayer: MediaPlayer? = null
    private var preloadedPlayer: MediaPlayer? = null
    private var preloadedTrack: TrackEntity? = null

    private val audioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_MEDIA)
        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
        .build()

    fun playTrack(track: TrackEntity, newQueue: List<TrackEntity> = emptyList()) {
        val currentQueue = if (newQueue.isNotEmpty()) newQueue else if (_playbackState.value.queue.isNotEmpty()) _playbackState.value.queue else listOf(track)
        
        _playbackState.value = _playbackState.value.copy(
            currentTrack = track,
            isPlaying = true,
            currentPositionSec = 0,
            durationSec = if (track.durationSec > 0) track.durationSec else 30,
            queue = currentQueue,
            isBuffering = false
        )

        engineScope.launch {
            startAudioStream(track)
            preloadNextTrack(track, currentQueue)
            startProgressTicker()
            context?.let {
                com.example.player.MediaPlaybackService.startService(it, track.title, track.artist, true)
            }
        }
    }

    fun pause() {
        if (_playbackState.value.isPlaying) {
            togglePlayPause()
        }
    }

    fun togglePlayPause() {
        val current = _playbackState.value
        if (current.currentTrack == null) return

        val newIsPlaying = !current.isPlaying
        _playbackState.value = current.copy(isPlaying = newIsPlaying)

        try {
            if (newIsPlaying) {
                activePlayer?.let {
                    if (!it.isPlaying) {
                        it.start()
                    }
                }
                startProgressTicker()
                context?.let { ctx ->
                    current.currentTrack?.let { tr ->
                        com.example.player.MediaPlaybackService.startService(ctx, tr.title, tr.artist, true)
                    }
                }
            } else {
                activePlayer?.let {
                    if (it.isPlaying) {
                        it.pause()
                    }
                }
                progressJob?.cancel()
                context?.let { ctx ->
                    current.currentTrack?.let { tr ->
                        com.example.player.MediaPlaybackService.startService(ctx, tr.title, tr.artist, false)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "togglePlayPause error: ${e.message}")
        }
    }

    fun seekTo(positionSec: Int) {
        val current = _playbackState.value
        val clamped = positionSec.coerceIn(0, current.durationSec)
        _playbackState.value = current.copy(currentPositionSec = clamped)

        try {
            activePlayer?.seekTo(clamped * 1000)
        } catch (e: Exception) {
            Log.e(TAG, "seekTo error: ${e.message}")
        }
    }

    fun next() {
        val current = _playbackState.value
        if (current.queue.isEmpty()) return

        val currentIndex = current.queue.indexOfFirst { it.id == current.currentTrack?.id }
        val nextIndex = if (current.isShuffle) {
            val validIndices = current.queue.indices.filter { it != currentIndex }
            if (validIndices.isNotEmpty()) validIndices.random() else 0
        } else {
            if (currentIndex + 1 < current.queue.size) currentIndex + 1 else if (current.repeatMode == RepeatMode.ALL) 0 else -1
        }

        if (nextIndex in current.queue.indices) {
            val nextTrack = current.queue[nextIndex]
            playTrack(nextTrack, current.queue)
        } else {
            // End of queue reached
            _playbackState.value = current.copy(isPlaying = false, currentPositionSec = 0)
            stopAndReleasePlayer()
            progressJob?.cancel()
        }
    }

    fun previous() {
        val current = _playbackState.value
        if (current.queue.isEmpty()) return

        if (current.currentPositionSec > 3) {
            seekTo(0)
            return
        }

        val currentIndex = current.queue.indexOfFirst { it.id == current.currentTrack?.id }
        val prevIndex = if (currentIndex - 1 >= 0) currentIndex - 1 else if (current.repeatMode == RepeatMode.ALL) current.queue.size - 1 else 0
        if (prevIndex in current.queue.indices) {
            playTrack(current.queue[prevIndex], current.queue)
        }
    }

    fun toggleShuffle() {
        _playbackState.value = _playbackState.value.copy(isShuffle = !_playbackState.value.isShuffle)
    }

    fun toggleRepeat() {
        val nextMode = when (_playbackState.value.repeatMode) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        _playbackState.value = _playbackState.value.copy(repeatMode = nextMode)
    }

    fun setPlaybackSpeed(speed: Float) {
        _playbackState.value = _playbackState.value.copy(playbackSpeed = speed)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                activePlayer?.let {
                    if (it.isPlaying) {
                        val params = it.playbackParams
                        params.speed = speed
                        it.playbackParams = params
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "setPlaybackSpeed error: ${e.message}")
            }
        }
    }

    fun setAudioQuality(quality: String) {
        _playbackState.value = _playbackState.value.copy(audioQuality = quality)
    }

    private suspend fun startAudioStream(track: TrackEntity) = withContext(Dispatchers.IO) {
        try {
            // Check if this track was already preloaded into preloadedPlayer
            if (preloadedPlayer != null && preloadedTrack?.id == track.id) {
                val playerToActivate = preloadedPlayer
                preloadedPlayer = null
                preloadedTrack = null

                // Release old active player safely
                activePlayer?.let { old ->
                    try {
                        if (old.isPlaying) old.stop()
                        old.reset()
                        old.release()
                    } catch (_: Exception) {}
                }

                activePlayer = playerToActivate
                activePlayer?.start()
                applyPlaybackSpeedToActivePlayer()
                return@withContext
            }

            // Fresh instantiation for track
            val resolvedUrl = com.example.data.remote.AudioStreamResolver.resolveTrackStream(track, context)

            val player = MediaPlayer().apply {
                setAudioAttributes(audioAttributes)
            }

            player.setDataSource(resolvedUrl)
            player.setOnPreparedListener { mp ->
                try {
                    mp.start()
                    applyPlaybackSpeedToActivePlayer()
                    val actualDurationSec = (mp.duration / 1000).coerceAtLeast(1)
                    _playbackState.value = _playbackState.value.copy(
                        durationSec = actualDurationSec,
                        isBuffering = false
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Player start error: ${e.message}")
                }
            }

            player.setOnCompletionListener {
                val state = _playbackState.value
                state.currentTrack?.let { onTrackPlayed(it, state.durationSec) }
                if (state.repeatMode == RepeatMode.ONE) {
                    seekTo(0)
                    activePlayer?.start()
                } else {
                    next()
                }
            }

            player.setOnErrorListener { _, what, extra ->
                Log.w(TAG, "MediaPlayer error: what=$what extra=$extra")
                _playbackState.value = _playbackState.value.copy(isBuffering = false)
                true
            }

            // Release previous active player
            activePlayer?.let { old ->
                try {
                    if (old.isPlaying) old.stop()
                    old.reset()
                    old.release()
                } catch (_: Exception) {}
            }

            activePlayer = player
            player.prepareAsync()

        } catch (e: Exception) {
            Log.e(TAG, "Failed to start audio stream: ${e.message}")
        }
    }

    private suspend fun preloadNextTrack(currentTrack: TrackEntity, queue: List<TrackEntity>) = withContext(Dispatchers.IO) {
        if (queue.size <= 1) return@withContext

        val currentIndex = queue.indexOfFirst { it.id == currentTrack.id }
        val nextIndex = if (_playbackState.value.isShuffle) {
            val valid = queue.indices.filter { it != currentIndex }
            if (valid.isNotEmpty()) valid.random() else -1
        } else {
            if (currentIndex + 1 < queue.size) currentIndex + 1 else if (_playbackState.value.repeatMode == RepeatMode.ALL) 0 else -1
        }

        if (nextIndex !in queue.indices) return@withContext

        val nextTrack = queue[nextIndex]
        if (nextTrack.id == preloadedTrack?.id && preloadedPlayer != null) {
            // Already preloaded
            return@withContext
        }

        try {
            preloadedPlayer?.let { old ->
                try {
                    old.reset()
                    old.release()
                } catch (_: Exception) {}
            }

            val nextUrl = com.example.data.remote.AudioStreamResolver.resolveTrackStream(nextTrack, context)
            
            val preload = MediaPlayer().apply {
                setAudioAttributes(audioAttributes)
            }
            preload.setDataSource(nextUrl)
            preload.setOnCompletionListener {
                val state = _playbackState.value
                state.currentTrack?.let { onTrackPlayed(it, state.durationSec) }
                if (state.repeatMode == RepeatMode.ONE) {
                    seekTo(0)
                } else {
                    next()
                }
            }
            preload.setOnErrorListener { _, _, _ -> true }
            preload.prepareAsync()

            preloadedPlayer = preload
            preloadedTrack = nextTrack
            Log.d(TAG, "Preloaded next track '${nextTrack.title}' for instant switching")
        } catch (e: Exception) {
            Log.w(TAG, "Preload failed: ${e.message}")
        }
    }

    private fun applyPlaybackSpeedToActivePlayer() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val speed = _playbackState.value.playbackSpeed
                activePlayer?.let {
                    val params = it.playbackParams
                    params.speed = speed
                    it.playbackParams = params
                }
            } catch (e: Exception) {
                Log.w(TAG, "Could not set playback speed: ${e.message}")
            }
        }
    }

    private fun startProgressTicker() {
        progressJob?.cancel()
        progressJob = engineScope.launch {
            while (isActive && _playbackState.value.isPlaying) {
                val stepMs = (500L / _playbackState.value.playbackSpeed).toLong().coerceAtLeast(150L)
                delay(stepMs)

                val state = _playbackState.value
                var realPos = state.currentPositionSec

                try {
                    activePlayer?.let {
                        if (it.isPlaying) {
                            realPos = (it.currentPosition / 1000).coerceAtLeast(0)
                        } else {
                            realPos += 1
                        }
                    } ?: run {
                        realPos += 1
                    }
                } catch (_: Exception) {
                    realPos += 1
                }

                // Generate dynamic realistic waveform levels based on energy rating
                val energy = state.currentTrack?.energyRating ?: 0.75f
                val newWaveform = List(24) {
                    (Random.nextFloat() * energy * 0.85f + 0.15f).coerceIn(0.1f, 1.0f)
                }

                if (realPos >= state.durationSec && state.durationSec > 0) {
                    state.currentTrack?.let { onTrackPlayed(it, state.durationSec) }
                    if (state.repeatMode == RepeatMode.ONE) {
                        seekTo(0)
                        _playbackState.value = state.copy(currentPositionSec = 0, waveformLevels = newWaveform)
                    } else {
                        next()
                    }
                } else {
                    _playbackState.value = state.copy(
                        currentPositionSec = realPos,
                        waveformLevels = newWaveform
                    )
                }
            }
        }
    }

    private fun stopAndReleasePlayer() {
        try {
            activePlayer?.let {
                if (it.isPlaying) it.stop()
                it.reset()
                it.release()
            }
            activePlayer = null

            preloadedPlayer?.let {
                it.reset()
                it.release()
            }
            preloadedPlayer = null
            preloadedTrack = null
        } catch (e: Exception) {
            Log.e(TAG, "stopAndReleasePlayer error: ${e.message}")
        }
    }

    fun release() {
        progressJob?.cancel()
        stopAndReleasePlayer()
    }
}
