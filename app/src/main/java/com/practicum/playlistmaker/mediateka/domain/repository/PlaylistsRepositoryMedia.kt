package com.practicum.playlistmaker.mediateka.domain.repository

import android.net.Uri
import androidx.lifecycle.LiveData
import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.core.models.domain.Playlist
import kotlinx.coroutines.flow.Flow

interface PlaylistsRepositoryMedia {
    fun getPlaylists(): Flow<List<Playlist>>
    suspend fun addPlaylist(playlist: Playlist): Long
    suspend fun deletePlaylist(playlistId: Long)
    suspend fun updatePlaylist(playlist: Playlist)
    suspend fun addTrackToPlaylist(playlistId: Long, track: Track)
    suspend fun safeCopyToPrivateStorage(sourcePath: String): String?
}