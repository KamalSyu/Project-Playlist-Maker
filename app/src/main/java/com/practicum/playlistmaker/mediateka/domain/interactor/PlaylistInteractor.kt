package com.practicum.playlistmaker.mediateka.domain.interactor

import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.core.models.domain.AddTrackStatus

interface PlaylistInteractor {
    suspend fun addTrackToPlaylist(playlistId: String, track: Track): AddTrackStatus
    suspend fun renamePlaylist(playlistId: String, newName: String)
    suspend fun deletePlaylist(playlistId: String)
    suspend fun createPlaylist(name: String, coverPath: String?)
    suspend fun getPlaylistById(playlistId: Long): com.practicum.playlistmaker.core.models.domain.Playlist?
    suspend fun updatePlaylistFull(playlistId: Long, name: String, description: String?, coverPath: String?)
}




