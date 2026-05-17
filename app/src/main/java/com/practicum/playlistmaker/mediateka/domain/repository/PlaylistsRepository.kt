package com.practicum.playlistmaker.mediateka.domain.repository

import com.practicum.playlistmaker.mediateka.domain.model.PlaylistData

interface PlaylistsRepository {
    suspend fun addPlaylist(playlist: PlaylistData) : Long
    suspend fun getPlaylists(): List<PlaylistData>
    suspend fun deletePlaylist(playlistId: Long)
    suspend fun updatePlaylist(playlist: PlaylistData)
}