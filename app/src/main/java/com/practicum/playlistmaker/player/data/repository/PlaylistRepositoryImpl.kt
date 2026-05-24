package com.practicum.playlistmaker.player.data.repository

import android.content.Context
import android.util.Log
import com.practicum.playlistmaker.core.models.domain.Playlist
import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.core.models.domain.AddTrackStatus
import com.practicum.playlistmaker.mediateka.data.db.PlaylistsDao
import com.practicum.playlistmaker.mediateka.data.mapper.toDomain
import com.practicum.playlistmaker.player.domain.model.PlaylistForPlayer
import com.practicum.playlistmaker.player.domain.repository.PlaylistRepository
import com.practicum.playlistmaker.mediateka.domain.repository.PlaylistsRepositoryMedia
import com.practicum.playlistmaker.player.data.storage.FileStorageService
import kotlinx.coroutines.flow.first

class PlaylistRepositoryImpl(
    private val playlistDao: PlaylistsDao,
    private val playlistsRepositoryMedia: PlaylistsRepositoryMedia,
    private val context: Context
) : PlaylistRepository {

    private val fileStorageService = FileStorageService(context)

    override suspend fun getPlaylists(): List<PlaylistForPlayer> {
        return playlistDao.getAllPlaylists()
            .first()
            .map { playlistEntity ->
                val domainPlaylist = playlistEntity.toDomain()
                PlaylistForPlayer.fromDomain(domainPlaylist)
            }
    }

    override suspend fun addTrackToPlaylist(playlistId: String, track: Track): AddTrackStatus {
        val playlistIdLong = playlistId.toLongOrNull() ?: return AddTrackStatus.ERROR
        return try {
            playlistsRepositoryMedia.addTrackToPlaylist(playlistIdLong, track)
            AddTrackStatus.SUCCESS
        } catch (e: Exception) {
            Log.e("PlaylistRepositoryImpl", "Ошибка при делегировании добавления трека", e)
            AddTrackStatus.ERROR
        }
    }



    override suspend fun createPlaylist(
        name: String,
        coverPath: String? = null,
        description: String? = null
    ): Result<String> {
        return try {
            if (name.isBlank()) {
                return Result.failure(IllegalArgumentException("Название плейлиста не может быть пустым"))
            }

            val savedCoverPath = coverPath?.let { safeCopyToPrivateStorage(it) }
            val newDomainPlaylist = Playlist(
                id = "",
                name = name,
                coverPath = savedCoverPath,
                trackCount = 0,
                description = description,
                createdAt = System.currentTimeMillis()
            )
            val playlistId = playlistsRepositoryMedia.addPlaylist(newDomainPlaylist)
            Result.success(playlistId)
        } catch (e: Exception) {
            Log.e("PlaylistRepositoryImpl", "Ошибка при создании плейлиста", e)
            Result.failure(e)
        }
    }

    /**
     * Безопасное копирование файла обложки в приватное хранилище.
     * Возвращает путь к скопированному файлу или null, если копирование не удалось.
     */
    private suspend fun safeCopyToPrivateStorage(originalPath: String): String? {
        return try {
            fileStorageService.copyToPrivateStorage(originalPath)
        } catch (e: Exception) {
            Log.w("PlaylistRepositoryImpl", "Не удалось скопировать обложку: $originalPath", e)
            null
        }
    }
}
