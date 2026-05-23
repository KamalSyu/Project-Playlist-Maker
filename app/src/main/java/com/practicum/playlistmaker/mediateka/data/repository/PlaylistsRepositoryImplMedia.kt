package com.practicum.playlistmaker.mediateka.data.repository

import androidx.lifecycle.LiveData
import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.core.models.domain.Playlist
import com.practicum.playlistmaker.mediateka.data.db.PlaylistTrackEntity
import com.practicum.playlistmaker.mediateka.data.db.PlaylistsDao
import com.practicum.playlistmaker.mediateka.data.mapper.toDomain
import com.practicum.playlistmaker.mediateka.data.mapper.toEntity
import com.practicum.playlistmaker.mediateka.domain.repository.PlaylistsRepositoryMedia
import com.practicum.playlistmaker.player.data.repository.AddTrackStatus
import kotlinx.coroutines.flow.map

class PlaylistsRepositoryImplMedia(
    private val dao: PlaylistsDao
) : PlaylistsRepositoryMedia {


    override fun getPlaylists(): LiveData<List<Playlist>> {
        return dao.getAllPlaylists().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun addPlaylist(playlist: Playlist): Long {
        val entity = playlist.toEntity()
        return dao.insert(entity)
    }

    override suspend fun updatePlaylist(playlist: Playlist) {
        val entity = playlist.toEntity()
        dao.update(entity)
    }

    override suspend fun deletePlaylist(playlistId: String) {
        val id = playlistId.toLongOrNull()
            ?: throw IllegalArgumentException("Invalid playlist ID: $playlistId")
        dao.delete(id)
    }
    override suspend fun addTrackToPlaylist(playlistId: String, track: Track): AddTrackStatus {
        playlistDao.insertTrackToPlaylist(PlaylistTrackEntity(playlistId.toLong(), track.trackId))
        return AddTrackStatus.SUCCESS
    }

}