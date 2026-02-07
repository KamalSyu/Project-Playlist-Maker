package com.practicum.playlistmaker.domain.repository

import com.practicum.playlistmaker.domain.model.Track

interface HistoryRepository {
    suspend fun addTrack(track: Track)
    suspend fun getHistory(): List<Track>
    suspend fun clearHistory()
}