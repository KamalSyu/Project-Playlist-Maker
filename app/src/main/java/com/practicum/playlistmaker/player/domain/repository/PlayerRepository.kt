package com.practicum.playlistmaker.player.domain.repository

interface PlayerRepository {
    suspend fun prepare(url: String?)
    suspend fun play()
    suspend fun pause()
    suspend fun stop()
    suspend fun reset()
    suspend fun playWithPosition(resumePosition: Long?)
    fun seekTo(position: Long)
    fun isPlaying(): Boolean
    fun getCurrentPosition(): Long
    fun setOnCompletionListener(listener: () -> Unit)
}