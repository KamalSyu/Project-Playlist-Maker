package com.practicum.playlistmaker.mediateka.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.core.models.domain.Playlist
import com.practicum.playlistmaker.mediateka.data.db.PlaylistTrackEntity
import com.practicum.playlistmaker.mediateka.data.db.PlaylistsDao
import com.practicum.playlistmaker.mediateka.data.mapper.toDomain
import com.practicum.playlistmaker.mediateka.data.mapper.toEntity
import com.practicum.playlistmaker.mediateka.domain.repository.PlaylistsRepositoryMedia
import com.practicum.playlistmaker.player.data.storage.FileStorageService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID

class PlaylistsRepositoryImplMedia(
    private val dao: PlaylistsDao,
    private val context: Context
) : PlaylistsRepositoryMedia {

    private val fileStorageService = FileStorageService(context)

    override suspend fun safeCopyToPrivateStorage(sourcePath: String): String? {
        return try {
            fileStorageService.copyToPrivateStorage(sourcePath)
        } catch (e: Exception) {
            Log.w("PlaylistsRepositoryImplMedia", "Не удалось скопировать обложку: $sourcePath", e)
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
        dao.insertPlaylist(entity)
    }


    override suspend fun updatePlaylist(playlist: Playlist) = withContext(Dispatchers.IO) {
        val entity = playlist.toEntity()
        dao.updatePlaylist(entity)
    }

    override suspend fun deletePlaylist(playlistId: Long) = withContext(Dispatchers.IO) {
        dao.deletePlaylistById(playlistId)
    }

    override suspend fun addTrackToPlaylist(playlistId: Long, track: Track) = withContext(Dispatchers.IO) {
        // Проверяем, есть ли трек в плейлисте
        val isTrackPresent = dao.isTrackInPlaylist(playlistId, track.trackId) > 0
        if (isTrackPresent) {
            throw Exception("Трек уже добавлен в плейлист")
        }

        // Создаём сущность для сохранения связи трек-плейлист
        val playlistTrackEntity = PlaylistTrackEntity(
            id = UUID.randomUUID().mostSignificantBits and Long.MAX_VALUE,
            playlistId = playlistId,
            title = track.trackName,
            artist = track.artistName,
            duration = ((track.trackTimeMillis ?: 0L) / 1000).toInt(),
            addedAt = System.currentTimeMillis()
        )

        // Добавляем запись в таблицу связей
        dao.insertTrackToPlaylist(playlistTrackEntity)

        // Обновляем счётчик треков в плейлисте
        dao.incrementTrackCount(playlistId)
    }
}
