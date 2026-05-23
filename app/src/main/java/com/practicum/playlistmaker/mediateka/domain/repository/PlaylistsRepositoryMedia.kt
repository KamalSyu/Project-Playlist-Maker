package com.practicum.playlistmaker.mediateka.domain.repository

import androidx.lifecycle.LiveData
import com.practicum.playlistmaker.core.models.domain.Playlist

interface PlaylistsRepositoryMedia {
    suspend fun addPlaylist(playlist: Playlist): Long
    fun getPlaylists(): LiveData<List<Playlist>>
    suspend fun deletePlaylist(playlistId: String)
    suspend fun updatePlaylist(playlist: Playlist)
}