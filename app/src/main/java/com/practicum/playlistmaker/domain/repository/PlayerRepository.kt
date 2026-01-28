package com.practicum.playlistmaker.domain.repository

interface PlayerRepository {
    suspend fun prepare(url: String?)
    suspend fun play()
    suspend fun pause()
    suspend fun stop()
    fun isPlaying(): Boolean
    fun getCurrentPosition(): Long
}
