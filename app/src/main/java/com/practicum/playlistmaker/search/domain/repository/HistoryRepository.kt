package com.practicum.playlistmaker.search.domain.repository

import com.practicum.playlistmaker.core.models.Track

interface HistoryRepository {
    suspend fun addTrack(track: Track)
    suspend fun getHistory(): List<Track>
    suspend fun clearHistory()
}