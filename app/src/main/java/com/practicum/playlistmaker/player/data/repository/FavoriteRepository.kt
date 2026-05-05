package com.practicum.playlistmaker.player.data.repository

import com.practicum.playlistmaker.core.models.Track
import kotlinx.coroutines.flow.Flow

interface FavoriteRepository {

    suspend fun addToFavorites(track: Track)
    suspend fun removeFromFavorites(trackId: String)
    suspend fun isTrackFavorite(trackId: String): Boolean
    fun getFavoriteTracks(): Flow<List<Track>>
}
