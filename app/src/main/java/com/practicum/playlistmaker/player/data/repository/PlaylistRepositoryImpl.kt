package com.practicum.playlistmaker.player.data.repository

import android.content.Context
import android.util.Log
import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.core.models.domain.AddTrackStatus
import com.practicum.playlistmaker.mediateka.data.db.PlaylistsDao
import com.practicum.playlistmaker.mediateka.data.db.PlaylistEntity
import com.practicum.playlistmaker.mediateka.data.db.PlaylistTrackEntity
import com.practicum.playlistmaker.mediateka.data.mapper.toDomain
import com.practicum.playlistmaker.player.domain.model.PlaylistForPlayer
import com.practicum.playlistmaker.player.domain.repository.PlaylistRepository
import com.practicum.playlistmaker.player.data.storage.FileStorageService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID

class PlaylistRepositoryImpl(
    private val playlistDao: PlaylistsDao,
    private val context: Context
) : PlaylistRepository {
    private val fileStorageService = FileStorageService(context)
    override fun getPlaylists(): Flow<List<PlaylistForPlayer>> {
        return playlistDao.getAllPlaylists()
            .map { entities ->
                entities.map { playlistEntity ->
                    val domainPlaylist = playlistEntity.toDomain()
                    PlaylistForPlayer.fromDomain(domainPlaylist)
                }
            }
    }
    override suspend fun addTrackToPlaylist(playlistId: Long, track: Track): AddTrackStatus {
        return withContext(Dispatchers.IO) {
            try {
                val isTrackPresent = playlistDao.isTrackInPlaylist(playlistId, track.trackId) > 0
                if (isTrackPresent) {
                    return@withContext AddTrackStatus.ALREADY_EXISTS
                }
                val playlistTrackEntity = PlaylistTrackEntity(
                    id = UUID.randomUUID().mostSignificantBits and Long.MAX_VALUE,
                    playlistId = playlistId,
                    trackId = track.trackId,
                    title = track.trackName,
                    artist = track.artistName,
                    duration = ((track.trackTimeMillis ?: 0L) / 1000).toInt(),
                    addedAt = System.currentTimeMillis()
                )
                playlistDao.insertTrackToPlaylist(playlistTrackEntity)
                playlistDao.incrementTrackCount(playlistId)
                AddTrackStatus.SUCCESS
            } catch (e: Exception) {
                Log.e("PlaylistRepositoryImpl", "Ошибка при добавлении трека в плейлист", e)
                AddTrackStatus.ERROR
            }
        }
    }
    override suspend fun createPlaylist(
        name: String,
        coverPath: String?,
        description: String?
    ): String {
        if (name.isBlank()) {
            throw IllegalArgumentException("Название плейлиста не может быть пустым")
        }
        return withContext(Dispatchers.IO) {
            try {
                val savedCoverPath = coverPath?.let { safeCopyToPrivateStorage(it) }
                val entity = PlaylistEntity(
                    id = 0,
                    name = name,
                    description = description,
                    coverPath = savedCoverPath,
                    trackCount = 0,
                    createdAt = System.currentTimeMillis()
                )
                val playlistIdLong = playlistDao.insertPlaylist(entity)
                playlistIdLong.toString()
            } catch (e: Exception) {
                Log.e("PlaylistRepositoryImpl", "Ошибка при создании плейлиста", e)
                throw e
            }
        }
    }
    private suspend fun safeCopyToPrivateStorage(originalPath: String): String? {
        return try {
            fileStorageService.copyToPrivateStorage(originalPath)
        } catch (e: Exception) {
            Log.w("PlaylistRepositoryImpl", "Не удалось скопировать обложку: $originalPath", e)
            null
        }
    }
    override suspend fun updatePlaylist(playlist: PlaylistForPlayer): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val existingEntity = playlistDao.getPlaylistById(playlist.id.toLong())
                    ?: return@withContext Result.failure(Exception("Плейлист не найден"))

                val entity = PlaylistEntity(
                    id = playlist.id.toLong(),
                    name = playlist.name,
                    description = existingEntity.description,
                    coverPath = playlist.coverPath,
                    trackCount = playlist.trackCount,
                    createdAt = existingEntity.createdAt
                )
                playlistDao.updatePlaylist(entity)
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e("PlaylistRepositoryImpl", "Ошибка при обновлении плейлиста", e)
                Result.failure(e)
            }
        }
    }
}
