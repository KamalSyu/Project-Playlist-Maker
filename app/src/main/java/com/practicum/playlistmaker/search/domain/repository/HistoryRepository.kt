package com.practicum.playlistmaker.search.domain.repository

import com.practicum.playlistmaker.core.models.Track
import kotlinx.coroutines.flow.Flow

interface HistoryRepository {
    suspend fun addTrack(track: Track)
    fun getHistory(): Flow<List<Track>>
    suspend fun clearHistory()
}