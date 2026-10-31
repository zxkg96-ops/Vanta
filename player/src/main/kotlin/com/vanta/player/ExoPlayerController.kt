package com.vanta.player

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.vanta.core.PlayerController
import com.vanta.core.PlayerState
import com.vanta.core.RepeatMode
import com.vanta.core.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Реальный плеер на Media3 ExoPlayer.
 * Управляет очередью, play/pause, seek, repeat, shuffle.
 */
class ExoPlayerController(
    context: Context
) : PlayerController {

    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val player: ExoPlayer = ExoPlayer.Builder(appContext).build().also { p ->
        p.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _state.update { it.copy(isPlaying = isPlaying) }
                if (isPlaying) startPositionUpdates() else stopPositionUpdates()
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    when (_state.value.repeat) {
                        RepeatMode.ONE -> {
                            p.seekTo(0)
                            p.play()
                        }
                        RepeatMode.ALL -> next()
                        RepeatMode.OFF -> {
                            if (p.hasNextMediaItem()) next()
                            else _state.update { it.copy(isPlaying = false) }
                        }
                    }
                }
                syncFromPlayer()
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                syncFromPlayer()
            }
        })
    }

    private val _state = MutableStateFlow(PlayerState())
    override val state: StateFlow<PlayerState> = _state.asStateFlow()

    private var positionJob: Job? = null
    private var queue: List<Track> = emptyList()

    private fun startPositionUpdates() {
        positionJob?.cancel()
        positionJob = scope.launch {
            while (isActive) {
                _state.update {
                    it.copy(
                        positionMs = player.currentPosition.coerceAtLeast(0),
                        durationMs = player.duration.coerceAtLeast(0)
                    )
                }
                delay(250)
            }
        }
    }

    private fun stopPositionUpdates() {
        positionJob?.cancel()
        positionJob = null
    }

    private fun syncFromPlayer() {
        val idx = player.currentMediaItemIndex
        _state.update {
            it.copy(
                queue = queue,
                currentIndex = if (queue.isEmpty()) -1 else idx.coerceIn(0, queue.lastIndex),
                isPlaying = player.isPlaying,
                positionMs = player.currentPosition.coerceAtLeast(0),
                durationMs = player.duration.coerceAtLeast(0),
                error = null
            )
        }
    }

    private fun Track.toMediaItem(): MediaItem {
        val meta = MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(artist)
            .setAlbumTitle(album)
            .build()
        return MediaItem.Builder()
            .setUri(Uri.parse(uri))
            .setMediaId(id.toString())
            .setMediaMetadata(meta)
            .build()
    }

    private fun setQueueInternal(tracks: List<Track>, startIndex: Int) {
        queue = tracks
        player.stop()
        player.clearMediaItems()
        if (tracks.isEmpty()) {
            _state.update { PlayerState() }
            return
        }
        val items = tracks.map { it.toMediaItem() }
        player.setMediaItems(items, startIndex.coerceIn(0, tracks.lastIndex), 0L)
        player.prepare()
        player.play()
        syncFromPlayer()
    }

    override fun openUri(uri: String) {
        val track = Track(
            id = uri.hashCode().toLong(),
            uri = uri,
            title = uri.substringAfterLast('/').substringBefore('?').ifBlank { "Audio" },
            artist = null,
            album = null,
            albumId = null,
            genre = null,
            artUri = null,
            durationMs = 0,
            folder = null
        )
        playTracks(listOf(track), 0)
    }

    override fun playTracks(tracks: List<Track>, startIndex: Int) {
        setQueueInternal(tracks, startIndex)
    }

    override fun playIndex(i: Int) {
        if (i !in queue.indices) return
        player.seekTo(i, 0L)
        player.play()
        syncFromPlayer()
    }

    override fun play() {
        player.play()
    }

    override fun pause() {
        player.pause()
    }

    override fun toggle() {
        if (player.isPlaying) player.pause() else player.play()
    }

    override fun next() {
        if (player.hasNextMediaItem()) {
            player.seekToNextMediaItem()
            player.play()
        } else if (_state.value.repeat == RepeatMode.ALL && queue.isNotEmpty()) {
            player.seekTo(0, 0L)
            player.play()
        }
        syncFromPlayer()
    }

    override fun prev() {
        if (player.currentPosition > 3000) {
            player.seekTo(0)
        } else if (player.hasPreviousMediaItem()) {
            player.seekToPreviousMediaItem()
            player.play()
        } else {
            player.seekTo(0)
        }
        syncFromPlayer()
    }

    override fun seekTo(ms: Long) {
        player.seekTo(ms.coerceAtLeast(0))
        _state.update { it.copy(positionMs = ms.coerceAtLeast(0)) }
    }

    override fun setSpeed(v: Float) {
        player.setPlaybackSpeed(v.coerceIn(0.5f, 2f))
        _state.update { it.copy(speed = v.coerceIn(0.5f, 2f)) }
    }

    override fun setRepeat(m: RepeatMode) {
        player.repeatMode = when (m) {
            RepeatMode.OFF -> Player.REPEAT_MODE_OFF
            RepeatMode.ONE -> Player.REPEAT_MODE_ONE
            RepeatMode.ALL -> Player.REPEAT_MODE_ALL
        }
        _state.update { it.copy(repeat = m) }
    }

    override fun setShuffle(on: Boolean) {
        player.shuffleModeEnabled = on
        _state.update { it.copy(shuffle = on) }
    }

    override fun setSleepTimer(minutes: Int?) {
        // Simple implementation: store only; full timer can be added later
        _state.update {
            it.copy(sleepTimerLeftMs = minutes?.let { m -> m * 60_000L })
        }
    }

    override fun addToQueue(track: Track) {
        queue = queue + track
        player.addMediaItem(track.toMediaItem())
        syncFromPlayer()
    }

    override fun playNext(track: Track) {
        val idx = player.currentMediaItemIndex
        val insertAt = (idx + 1).coerceAtMost(queue.size)
        queue = queue.toMutableList().also { it.add(insertAt, track) }
        player.addMediaItem(insertAt, track.toMediaItem())
        syncFromPlayer()
    }

    override fun removeFromQueue(index: Int) {
        if (index !in queue.indices) return
        queue = queue.toMutableList().also { it.removeAt(index) }
        player.removeMediaItem(index)
        syncFromPlayer()
    }

    override fun moveInQueue(from: Int, to: Int) {
        if (from !in queue.indices || to !in queue.indices || from == to) return
        val q = queue.toMutableList()
        val item = q.removeAt(from)
        q.add(to, item)
        queue = q
        player.moveMediaItem(from, to)
        syncFromPlayer()
    }

    // EQ stubs — real audio effects can be added later
    override fun setEqEnabled(on: Boolean) {}
    override fun setEqBand(index: Int, gainDb: Float) {}
    override fun setBassBoost(level: Int) {}
    override fun setTreble(gainDb: Float) {}
    override fun setBalance(value: Float) {}
    override fun applyPreset(name: String) {}

    fun release() {
        stopPositionUpdates()
        player.release()
    }
}
