package com.practicum.playlistmaker.mediateka.domain.repository

import android.net.Uri
import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.core.models.domain.Playlist
import com.practicum.playlistmaker.mediateka.data.db.PlaylistEntity
import com.practicum.playlistmaker.mediateka.data.db.PlaylistTrackEntity
import kotlinx.coroutines.flow.Flow

interface PlaylistsRepositoryMedia {
    fun getPlaylists(): Flow<List<Playlist>>
    suspend fun addPlaylist(playlist: Playlist): Long
    suspend fun deletePlaylist(playlistId: Long)
    suspend fun updatePlaylist(playlist: Playlist)
    suspend fun addTrackToPlaylist(playlistId: Long, track: Track)
    suspend fun safeCopyToPrivateStorage(sourcePath: String): String?
    suspend fun safeCopyToPrivateStorageFromUri(uri: Uri): String?
    suspend fun getPlaylistById(playlistId: Long): PlaylistEntity?
    suspend fun getTrackDurationsSeconds(playlistId: Long): List<Int>
    suspend fun getPlaylistTracks(playlistId: Long): List<PlaylistTrackEntity>
    suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: String)
    suspend fun deleteTrackIfUnused(trackId: String)
    suspend fun deletePlaylistAndCleanup(playlistId: Long)

}