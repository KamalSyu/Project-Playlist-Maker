package com.practicum.playlistmaker.mediateka.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.core.models.domain.Playlist
import com.practicum.playlistmaker.mediateka.data.db.PlaylistEntity
import com.practicum.playlistmaker.mediateka.data.db.PlaylistTrackEntity
import com.practicum.playlistmaker.mediateka.data.db.PlaylistsDao
import com.practicum.playlistmaker.mediateka.data.mapper.toDomain
import com.practicum.playlistmaker.mediateka.data.mapper.toEntity
import com.practicum.playlistmaker.mediateka.domain.repository.PlaylistsRepositoryMedia
import com.practicum.playlistmaker.player.data.storage.FileStorageService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class PlaylistsRepositoryImplMedia(
    private val dao: PlaylistsDao,
    private val context: Context  // <-- ВАЖНО: сохраняем context как приватное поле
) : PlaylistsRepositoryMedia {

    private val fileStorageService = FileStorageService(context)

    override suspend fun safeCopyToPrivateStorage(sourcePath: String): String? {
        return try {
            val result = fileStorageService.copyToPrivateStorage(sourcePath)
            Log.d("RepoCheck", "sourcePath (входной): $sourcePath")
            Log.d("RepoCheck", "result from FileStorageService: $result")
            if (!result.isNullOrEmpty()) {
                val file = File(result)
                Log.d("RepoCheck", "file.exists(): ${file.exists()}, length: ${file.length()}")
                if (!file.exists()) {
                    Log.w("RepoCheck", "WARNING: FileStorageService вернул путь, но файла по нему нет!")
                }
                if (file.length() == 0L) {
                    Log.w("RepoCheck", "WARNING: Файл существует, но он пустой (0 байт)!")
                }
            } else {
                Log.w("RepoCheck", "WARNING: FileStorageService вернул null или пустую строку")
            }
            result
        } catch (e: Exception) {
            Log.w("PlaylistsRepositoryImplMedia", "Не удалось скопировать обложку: $sourcePath", e)
            null
        }
    }

    // НОВЫЙ МЕТОД: принимает Uri и сам копирует в filesDir
    override suspend fun safeCopyToPrivateStorageFromUri(uri: Uri): String? {
        return try {
            // Получаем имя файла из URI или генерируем
            val fileName = uri.lastPathSegment ?: "cover_${System.currentTimeMillis()}"
            // Копируем сразу в filesDir (надёжно, не удалится при очистке кэша)
            val destFile = File(context.filesDir, fileName)

            context.contentResolver.openInputStream(uri)?.use { input ->
                destFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            Log.d(
                "RepoCheck",
                "Копия создана: ${destFile.absolutePath}, exists=${destFile.exists()}, size=${destFile.length()}"
            )

            if (destFile.exists() && destFile.length() > 0) {
                destFile.absolutePath
            } else {
                Log.w("RepoCheck", "Файл не создан или пустой")
                null
            }
        } catch (e: Exception) {
            Log.e("RepoCheck", "Ошибка копирования из Uri", e)
            null
        }
    }

    override fun getPlaylists(): Flow<List<Playlist>> {
        return dao.getAllPlaylists().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun addPlaylist(playlist: Playlist): Long = withContext(Dispatchers.IO) {
        require(playlist.name.isNotBlank()) { "Название плейлиста не может быть пустым" }
        val entity = playlist.toEntity()

        Log.d("RepoCheck", "Готов к вставке в БД entity.coverPath: ${entity.coverPath?.take(60)}")
        if (entity.coverPath.isNullOrEmpty()) {
            Log.e("RepoCheck", "ОШИБКА: entity.coverPath пуст перед insertPlaylist! playlist.name=${playlist.name}")
        }

        dao.insertPlaylist(entity)
    }

    override suspend fun updatePlaylist(playlist: Playlist) = withContext(Dispatchers.IO) {
        val entity = playlist.toEntity()
        Log.d("RepoCheck", "Обновление плейлиста, entity.coverPath: ${entity.coverPath?.take(60)}")
        dao.updatePlaylist(entity)
    }

    override suspend fun deletePlaylist(playlistId: Long) = withContext(Dispatchers.IO) {
        dao.deletePlaylistById(playlistId)
    }

    override suspend fun addTrackToPlaylist(playlistId: Long, track: Track) = withContext(Dispatchers.IO) {
        val isTrackPresent = dao.isTrackInPlaylist(playlistId, track.trackId) > 0
        if (isTrackPresent) {
            throw Exception("Трек уже добавлен в плейлист")
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
        dao.insertTrackToPlaylist(playlistTrackEntity)
        dao.incrementTrackCount(playlistId)
    }
    override suspend fun getPlaylistById(playlistId: Long): PlaylistEntity? =
        dao.getPlaylistById(playlistId)

    override suspend fun getTrackDurationsSeconds(playlistId: Long): List<Int> =
        dao.getTrackDurationsByPlaylistId(playlistId)

    override suspend fun getPlaylistTracks(playlistId: Long): List<PlaylistTrackEntity> =
        dao.getTracksByPlaylistId(playlistId)

    override suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: String) {
        dao.removeTrackFromPlaylist(playlistId, trackId)
    }

    override suspend fun deleteTrackIfUnused(trackId: String) {
        val count = dao.countPlaylistsWithTrack(trackId)
//        if (count == 0) {
//        }
    }

}
