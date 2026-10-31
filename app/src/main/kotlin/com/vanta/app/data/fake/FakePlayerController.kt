package com.vanta.app.data.fake

import com.vanta.core.EqualizerState
import com.vanta.core.PlayerController
import com.vanta.core.PlayerState
import com.vanta.core.RepeatMode
import com.vanta.core.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakePlayerController : PlayerController {
    private val mutableState = MutableStateFlow(PlayerState())
    override val state: StateFlow<PlayerState> = mutableState.asStateFlow()

    private fun update(block: (PlayerState) -> PlayerState) {
        mutableState.value = block(mutableState.value)
    }

    override fun openUri(uri: String) {
        val track = FakeCatalog.tracks.firstOrNull { it.uri == uri } ?: Track(
            id = -1L, uri = uri, title = uri.substringAfterLast('/').ifBlank { "Unknown track" },
            artist = null, album = null, albumId = null, genre = null, artUri = null,
            durationMs = 0L, folder = null
        )
        update { it.copy(queue = listOf(track), currentIndex = 0, isPlaying = true, positionMs = 0, durationMs = track.durationMs) }
    }

    override fun playTracks(tracks: List<Track>, startIndex: Int) {
        if (tracks.isEmpty()) return
        val index = startIndex.coerceIn(tracks.indices)
        update { it.copy(queue = tracks, currentIndex = index, isPlaying = true, positionMs = 0, durationMs = tracks[index].durationMs, error = null) }
    }

    override fun playIndex(i: Int) {
        val s = state.value
        if (i !in s.queue.indices) return
        update { it.copy(currentIndex = i, isPlaying = true, positionMs = 0, durationMs = it.queue[i].durationMs) }
    }

    override fun play() = update { if (it.queue.isEmpty()) it else it.copy(isPlaying = true) }
    override fun pause() = update { it.copy(isPlaying = false) }
    override fun toggle() = update { it.copy(isPlaying = !it.isPlaying && it.queue.isNotEmpty()) }

    override fun next() {
        update {
            if (it.queue.isEmpty()) it
            else {
                val next = when {
                    it.currentIndex < it.queue.lastIndex -> it.currentIndex + 1
                    it.repeat == RepeatMode.ALL -> 0
                    else -> return@update it.copy(isPlaying = false)
                }
                it.copy(currentIndex = next, positionMs = 0, durationMs = it.queue[next].durationMs, isPlaying = true)
            }
        }
    }

    override fun prev() {
        update {
            if (it.queue.isEmpty()) it
            else if (it.positionMs > 3_000L) it.copy(positionMs = 0)
            else {
                val previous = (it.currentIndex - 1).coerceAtLeast(0)
                it.copy(currentIndex = previous, positionMs = 0, durationMs = it.queue[previous].durationMs, isPlaying = true)
            }
        }
    }

    override fun seekTo(ms: Long) = update { it.copy(positionMs = ms.coerceIn(0L, it.durationMs.coerceAtLeast(0L))) }
    override fun setSpeed(v: Float) = update { it.copy(speed = v.coerceIn(0.5f, 2f)) }
    override fun setRepeat(m: RepeatMode) = update { it.copy(repeat = m) }
    override fun setShuffle(on: Boolean) = update { it.copy(shuffle = on) }
    override fun setSleepTimer(minutes: Int?) = update { it.copy(sleepTimerLeftMs = minutes?.coerceAtLeast(1)?.times(60_000L)) }

    override fun addToQueue(track: Track) = update { it.copy(queue = it.queue + track) }
    override fun playNext(track: Track) = update {
        val insertAt = (it.currentIndex + 1).coerceIn(0, it.queue.size)
        it.copy(queue = it.queue.toMutableList().apply { add(insertAt, track) })
    }
    override fun removeFromQueue(index: Int) = update {
        if (index !in it.queue.indices) it else {
            val queue = it.queue.toMutableList().apply { removeAt(index) }
            val current = when {
                queue.isEmpty() -> -1
                index < it.currentIndex -> it.currentIndex - 1
                index == it.currentIndex -> it.currentIndex.coerceAtMost(queue.lastIndex)
                else -> it.currentIndex
            }
            it.copy(queue = queue, currentIndex = current, isPlaying = queue.isNotEmpty() && it.isPlaying,
                durationMs = queue.getOrNull(current)?.durationMs ?: 0L)
        }
    }
    override fun moveInQueue(from: Int, to: Int) = update {
        if (from !in it.queue.indices || to !in it.queue.indices || from == to) it else {
            val q = it.queue.toMutableList()
            val item = q.removeAt(from)
            q.add(to, item)
            val current = when {
                it.currentIndex == from -> to
                from < it.currentIndex && to >= it.currentIndex -> it.currentIndex - 1
                from > it.currentIndex && to <= it.currentIndex -> it.currentIndex + 1
                else -> it.currentIndex
            }
            it.copy(queue = q, currentIndex = current)
        }
    }

    override fun setEqEnabled(on: Boolean) = update { it.copy(equalizer = it.equalizer.copy(enabled = on)) }
    override fun setEqBand(index: Int, gainDb: Float) = update {
        val bands = it.equalizer.bandsDb.toMutableList()
        if (index !in bands.indices) return@update it
        bands[index] = gainDb.coerceIn(it.equalizer.minDb, it.equalizer.maxDb)
        it.copy(equalizer = it.equalizer.copy(bandsDb = bands))
    }
    override fun setBassBoost(level: Int) = update { it.copy(equalizer = it.equalizer.copy(bassBoost = level.coerceIn(0, 100))) }
    override fun setTreble(gainDb: Float) = update { it.copy(equalizer = it.equalizer.copy(trebleDb = gainDb.coerceIn(-12f, 12f))) }
    override fun setBalance(value: Float) = update { it.copy(equalizer = it.equalizer.copy(balance = value.coerceIn(-1f, 1f))) }
    override fun applyPreset(name: String) = update {
        if (name !in it.equalizer.presets) it else it.copy(equalizer = it.equalizer.copy(currentPreset = name))
    }

    init {
        mutableState.value = PlayerState(
            equalizer = EqualizerState(
                supported = true, enabled = false,
                bandFreqsHz = listOf(60, 230, 910, 3600, 14000),
                bandsDb = List(5) { 0f },
                bassBoostSupported = true,
                presets = listOf("Flat", "Bass", "Vocal", "Treble", "Classical"),
                currentPreset = "Flat"
            )
        )
    }
}
