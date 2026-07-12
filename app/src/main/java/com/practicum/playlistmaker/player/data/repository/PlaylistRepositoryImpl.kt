package com.practicum.playlistmaker.player.data.repository

import android.content.Context
import android.net.Uri
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
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class PlaylistRepositoryImpl(
    private val playlistDao: PlaylistsDao,
    context: Context
) : PlaylistRepository {

    private val fileStorageService = FileStorageService(context)
    private val contentResolver = context.contentResolver
    private val context = context  // <-- теперь context явно сохранён как поле класса

    override fun getPlaylists(): Flow<List<PlaylistForPlayer>> {
        return playlistDao.getAllPlaylists()
            .map { entities ->
                val playlists = entities.map { playlistEntity ->
                    val domainPlaylist = playlistEntity.toDomain()
                    PlaylistForPlayer.fromDomain(domainPlaylist)
                }
                Log.d("PlaylistRepositoryImpl", "Количество загруженных плейлистов: ${playlists.size}")
                playlists.forEachIndexed { index, playlist ->
                    Log.d(
                        "PlaylistRepositoryImpl",
                        "Плейлист $index: name='${playlist.name}', trackCount=${playlist.trackCount}, coverPath='${playlist.coverPath}'"
                    )
                }
                playlists
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
                val savedCoverPath = coverPath?.let { path ->
                    Log.d("PlaylistRepoDebug", "Исходный coverPath: $path")
                    if (path.startsWith("content://")) {
                        Log.d("PlaylistRepoDebug", "Это content URI — будем копировать через copyUriToPrivateStorage")
                        copyUriToPrivateStorage(Uri.parse(path))
                    } else {
                        Log.d("PlaylistRepoDebug", "Это обычный путь — используем fileStorageService")
                        fileStorageService.copyToPrivateStorage(path)
                    }
                }
                Log.d("PlaylistRepoDebug", "Результат savedCoverPath: $savedCoverPath")


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

    /**
     * Копирует файл из URI (например, из Photopicker) в приватное хранилище приложения
     * и возвращает абсолютный путь к новому файлу.
     */
    private fun copyUriToPrivateStorage(uri: Uri): String? {
        return try {
            val fileName = "${System.currentTimeMillis()}_${UUID.randomUUID()}.jpg"
            // Используем context, который мы сохранили в поле класса
            val file = File(context.filesDir, fileName)

            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }

            file.absolutePath
        } catch (e: Exception) {
            Log.w("PlaylistRepositoryImpl", "Не удалось скопировать URI обложки: $uri", e)
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
