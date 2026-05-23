package com.practicum.playlistmaker.mediateka.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.practicum.playlistmaker.core.models.Playlist
import com.practicum.playlistmaker.mediateka.data.db.PlaylistEntity
import com.practicum.playlistmaker.mediateka.data.db.PlaylistsDao
import com.practicum.playlistmaker.mediateka.domain.model.PlaylistForMediateka
import com.practicum.playlistmaker.mediateka.domain.repository.PlaylistsRepository

class PlaylistsRepositoryImpl(
    private val dao: PlaylistsDao
) : PlaylistsRepository {

    override suspend fun addPlaylist(playlist: PlaylistForMediateka): Long {
        val domainPlaylist = PlaylistForMediateka.toDomain(playlist)
        val entity = PlaylistEntity(
            id = domainPlaylist.id.toLongOrNull() ?: System.currentTimeMillis(),
            name = domainPlaylist.name,
            description = domainPlaylist.description ?: "",
            coverPath = domainPlaylist.coverPath,
            trackIds = domainPlaylist.trackIds ?: "[]",
            trackCount = domainPlaylist.trackCount,
            createdAt = domainPlaylist.createdAt
        )
        return dao.insert(entity)
    }

    override fun getPlaylists(): LiveData<List<PlaylistForMediateka>> {
        return dao.getAllPlaylists().map { entities ->
            entities.map { entity ->
                val domainPlaylist = Playlist(
                    id = entity.id.toString(),
                    name = entity.name,
                    coverPath = entity.coverPath,
                    trackCount = entity.trackCount,
                    description = entity.description,
                    createdAt = entity.createdAt,
                    trackIds = entity.trackIds
                )
                PlaylistForMediateka.fromDomain(domainPlaylist)
            }
        }
    }

    override suspend fun deletePlaylist(playlistId: Long) {
        dao.delete(playlistId)
    }

    override suspend fun updatePlaylist(playlist: PlaylistForMediateka) {
        val domainPlaylist = PlaylistForMediateka.toDomain(playlist)
        val id = domainPlaylist.id.toLongOrNull()
        require(id != null) { "Invalid playlist ID: ${domainPlaylist.id}" }

        val entity = PlaylistEntity(
            id = id,
            name = domainPlaylist.name,
            description = domainPlaylist.description ?: "",
            coverPath = domainPlaylist.coverPath,
            trackIds = domainPlaylist.trackIds ?: "[]",
            trackCount = domainPlaylist.trackCount,
            createdAt = domainPlaylist.createdAt
        )
        dao.update(entity)
    }
}
