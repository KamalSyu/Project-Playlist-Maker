package com.practicum.playlistmaker.mediateka.domain.repository

import androidx.lifecycle.LiveData
import com.practicum.playlistmaker.mediateka.domain.model.PlaylistData

interface PlaylistsRepository {
    suspend fun addPlaylist(playlist: PlaylistData) : Long
    fun getPlaylists(): LiveData<List<PlaylistData>>
    suspend fun deletePlaylist(playlistId: Long)
    suspend fun updatePlaylist(playlist: PlaylistData)
}