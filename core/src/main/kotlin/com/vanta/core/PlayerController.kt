package com.vanta.core

import kotlinx.coroutines.flow.StateFlow

interface PlayerController {
    val state: StateFlow<PlayerState>

    fun openUri(uri: String)
    fun playTracks(tracks: List<Track>, startIndex: Int)
    fun playIndex(i: Int)
    fun play()
    fun pause()
    fun toggle()
    fun next()
    fun prev()
    fun seekTo(ms: Long)
    fun setSpeed(v: Float)
    fun setRepeat(m: RepeatMode)
    fun setShuffle(on: Boolean)
    fun setSleepTimer(minutes: Int?)

    fun addToQueue(track: Track)
    fun playNext(track: Track)
    fun removeFromQueue(index: Int)
    fun moveInQueue(from: Int, to: Int)

    fun setEqEnabled(on: Boolean)
    fun setEqBand(index: Int, gainDb: Float)
    fun setBassBoost(level: Int)
    fun setTreble(gainDb: Float)
    fun setBalance(value: Float)
    fun applyPreset(name: String)
}
