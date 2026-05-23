package com.practicum.playlistmaker.mediateka.domain.repository

import androidx.lifecycle.LiveData
import com.practicum.playlistmaker.mediateka.domain.model.PlaylistForMediateka

interface PlaylistsRepository {
    suspend fun addPlaylist(playlist: PlaylistForMediateka) : Long
    fun getPlaylists(): LiveData<List<PlaylistForMediateka>>
    suspend fun deletePlaylist(playlistId: Long)
    suspend fun updatePlaylist(playlist: PlaylistForMediateka)
}