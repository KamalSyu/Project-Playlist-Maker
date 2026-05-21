package com.practicum.playlistmaker.mediateka.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.practicum.playlistmaker.mediateka.data.db.PlaylistEntity
import com.practicum.playlistmaker.mediateka.data.db.PlaylistsDao
import com.practicum.playlistmaker.mediateka.domain.model.PlaylistData
import com.practicum.playlistmaker.mediateka.domain.repository.PlaylistsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class PlaylistsRepositoryImpl(
    private val dao: PlaylistsDao
) : PlaylistsRepository {

    override suspend fun addPlaylist(playlist: PlaylistData): Long {
        val entity = PlaylistEntity(
            id = if (playlist.id == 0L) 0 else playlist.id,
            name = playlist.name,
            description = playlist.description,
            coverPath = playlist.coverPath,
            trackIds = playlist.trackIds,
            trackCount = playlist.trackCount,
            createdAt = playlist.createdAt
        )
        return dao.insert(entity)
    }

    override fun getPlaylists(): LiveData<List<PlaylistData>> {
        return dao.getAllPlaylists().map { entities ->
            entities.map { entity ->
                PlaylistData(
                    id = entity.id,
                    name = entity.name,
                    description = entity.description,
                    coverPath = entity.coverPath,
                    trackIds = entity.trackIds,
                    trackCount = entity.trackCount,
                    createdAt = entity.createdAt
                )
            }
        }
    }

    override suspend fun deletePlaylist(playlistId: Long) {
        dao.delete(playlistId)
    }

    override suspend fun updatePlaylist(playlist: PlaylistData) {
        val entity = PlaylistEntity(
            id = playlist.id,
            name = playlist.name,
            description = playlist.description,
            coverPath = playlist.coverPath,
            trackIds = playlist.trackIds,
            trackCount = playlist.trackCount,
            createdAt = playlist.createdAt
        )
        dao.update(entity)
    }
}
