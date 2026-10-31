package com.vanta.player

import android.content.Context
import android.content.Intent
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.os.SystemClock
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import com.vanta.core.EqualizerState
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

    private val prefs = appContext.getSharedPreferences("vanta_player", Context.MODE_PRIVATE)

    private val player: ExoPlayer = ExoPlayer.Builder(appContext)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .build(),
            true
        )
        .setHandleAudioBecomingNoisy(true)
        .setWakeMode(C.WAKE_MODE_LOCAL)
        .build().also { p ->
        p.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _state.update { it.copy(isPlaying = isPlaying) }
                if (isPlaying) {
                    startPositionUpdates()
                    startPlaybackService()
                } else {
                    stopPositionUpdates()
                    saveSession()
                }
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
    private var sleepJob: Job? = null
    private var queue: List<Track> = emptyList()

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var currentPreset: String? = null

    /** Нужен сервису фонового воспроизведения (MediaSession). */
    val exoPlayer: ExoPlayer get() = player

    init {
        instance = this
        ensureEffects()
    }

    private fun startPlaybackService() {
        try {
            appContext.startService(Intent(appContext, VantaPlaybackService::class.java))
        } catch (e: Exception) {
            // приложение в фоне — сервис стартует при следующем действии
        }
    }

    private fun saveSession() {
        if (queue.isEmpty()) {
            prefs.edit().remove("ids").apply()
            return
        }
        prefs.edit()
            .putString("ids", queue.joinToString(",") { it.id.toString() })
            .putInt("index", player.currentMediaItemIndex)
            .putLong("pos", player.currentPosition.coerceAtLeast(0))
            .apply()
    }

    override fun restoreSession(library: List<Track>) {
        if (queue.isNotEmpty()) return
        val ids = prefs.getString("ids", null)?.split(",")?.mapNotNull { it.toLongOrNull() } ?: return
        val byId = library.associateBy { it.id }
        val tracks = ids.mapNotNull { byId[it] }
        if (tracks.isEmpty()) return
        val curId = ids.getOrNull(prefs.getInt("index", 0))
        val idx = tracks.indexOfFirst { it.id == curId }.coerceAtLeast(0)
        queue = tracks
        player.setMediaItems(tracks.map { it.toMediaItem() }, idx, prefs.getLong("pos", 0L))
        player.prepare()
        ensureEffects()
        syncFromPlayer()
    }

    private fun startPositionUpdates() {
        positionJob?.cancel()
        positionJob = scope.launch {
            var ticks = 0
            while (isActive) {
                _state.update {
                    it.copy(
                        positionMs = player.currentPosition.coerceAtLeast(0),
                        durationMs = player.duration.coerceAtLeast(0)
                    )
                }
                if (++ticks % 20 == 0) saveSession()
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
        saveSession()
    }

    private fun Track.toMediaItem(): MediaItem {
        val meta = MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(artist)
            .setAlbumTitle(album)
            .apply { artUri?.let { setArtworkUri(Uri.parse(it)) } }
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
            _state.update {
                it.copy(queue = emptyList(), currentIndex = -1, isPlaying = false, positionMs = 0, durationMs = 0)
            }
            saveSession()
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
        sleepJob?.cancel()
        sleepJob = null
        if (minutes == null) {
            _state.update { it.copy(sleepTimerLeftMs = null) }
            return
        }
        val end = SystemClock.elapsedRealtime() + minutes * 60_000L
        sleepJob = scope.launch {
            while (isActive) {
                val left = end - SystemClock.elapsedRealtime()
                if (left <= 0) {
                    player.pause()
                    _state.update { it.copy(sleepTimerLeftMs = null) }
                    break
                }
                _state.update { it.copy(sleepTimerLeftMs = left) }
                delay(1000)
            }
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

    // ---------- Эквалайзер (android.media.audiofx) ----------

    private fun ensureEffects() {
        if (equalizer != null) return
        val sid = player.audioSessionId
        if (sid == C.AUDIO_SESSION_ID_UNSET) return
        val on = prefs.getBoolean("eq_on", false)
        try {
            val eq = Equalizer(0, sid)
            val saved = prefs.getString("eq_bands", null)?.split(",")?.mapNotNull { it.toIntOrNull() }
            for (b in 0 until eq.numberOfBands.toInt()) {
                val lvl = saved?.getOrNull(b) ?: continue
                eq.setBandLevel(b.toShort(), lvl.toShort())
            }
            eq.setEnabled(on)
            equalizer = eq
        } catch (e: Exception) {
            equalizer = null
        }
        try {
            val bb = BassBoost(0, sid)
            if (bb.strengthSupported) bb.setStrength((prefs.getInt("eq_bass", 0) * 10).toShort())
            bb.setEnabled(on)
            bassBoost = bb
        } catch (e: Exception) {
            bassBoost = null
        }
        publishEq()
    }

    private fun publishEq() {
        val eq = equalizer ?: return
        try {
            val n = eq.numberOfBands.toInt()
            val range = eq.bandLevelRange
            val bbLevel = bassBoost?.roundedStrength?.toInt() ?: 0
            val st = EqualizerState(
                supported = true,
                enabled = eq.getEnabled(),
                bandFreqsHz = (0 until n).map { eq.getCenterFreq(it.toShort()) / 1000 },
                bandsDb = (0 until n).map { eq.getBandLevel(it.toShort()) / 100f },
                minDb = range[0] / 100f,
                maxDb = range[1] / 100f,
                bassBoostSupported = bassBoost?.strengthSupported == true,
                bassBoost = bbLevel / 10,
                presets = (0 until eq.numberOfPresets.toInt()).map { eq.getPresetName(it.toShort()) },
                currentPreset = currentPreset
            )
            _state.update { it.copy(equalizer = st) }
        } catch (e: Exception) {
            // эффект недоступен
        }
    }

    private fun saveBands() {
        val eq = equalizer ?: return
        val n = eq.numberOfBands.toInt()
        prefs.edit()
            .putString("eq_bands", (0 until n).joinToString(",") { eq.getBandLevel(it.toShort()).toString() })
            .apply()
    }

    override fun setEqEnabled(on: Boolean) {
        ensureEffects()
        try {
            equalizer?.setEnabled(on)
            bassBoost?.setEnabled(on)
        } catch (e: Exception) {
        }
        prefs.edit().putBoolean("eq_on", on).apply()
        publishEq()
    }

    override fun setEqBand(index: Int, gainDb: Float) {
        val eq = equalizer ?: return
        try {
            val range = eq.bandLevelRange
            val level = (gainDb * 100).toInt().coerceIn(range[0].toInt(), range[1].toInt())
            eq.setBandLevel(index.toShort(), level.toShort())
            currentPreset = null
            saveBands()
        } catch (e: Exception) {
        }
        publishEq()
    }

    override fun setBassBoost(level: Int) {
        val bb = bassBoost ?: return
        try {
            val l = level.coerceIn(0, 100)
            if (bb.strengthSupported) bb.setStrength((l * 10).toShort())
            prefs.edit().putInt("eq_bass", l).apply()
        } catch (e: Exception) {
        }
        publishEq()
    }

    override fun applyPreset(name: String) {
        val eq = equalizer ?: return
        try {
            val idx = (0 until eq.numberOfPresets.toInt()).firstOrNull { eq.getPresetName(it.toShort()) == name } ?: return
            eq.usePreset(idx.toShort())
            currentPreset = name
            saveBands()
        } catch (e: Exception) {
        }
        publishEq()
    }

    override fun setTreble(gainDb: Float) {}
    override fun setBalance(value: Float) {}

    fun release() {
        stopPositionUpdates()
        player.release()
    }

    companion object {
        @Volatile
        var instance: ExoPlayerController? = null
    }
}
