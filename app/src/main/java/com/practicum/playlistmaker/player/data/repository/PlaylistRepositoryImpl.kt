package com.practicum.playlistmaker.player.data.repository

import android.content.Context
import android.util.Log
import com.practicum.playlistmaker.core.models.domain.Playlist
import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.mediateka.data.db.PlaylistTrackEntity
import com.practicum.playlistmaker.mediateka.data.db.PlaylistsDao
import com.practicum.playlistmaker.mediateka.data.mapper.toDomain
import com.practicum.playlistmaker.mediateka.data.mapper.toEntity
import com.practicum.playlistmaker.player.domain.model.PlaylistForPlayer
import com.practicum.playlistmaker.player.domain.repository.PlaylistRepository
import com.practicum.playlistmaker.mediateka.domain.repository.PlaylistsRepositoryMedia
import com.practicum.playlistmaker.player.data.storage.FileStorageService
import kotlinx.coroutines.flow.first


enum class AddTrackStatus { SUCCESS, ALREADY_EXISTS, ERROR }

class PlaylistRepositoryImpl(
    private val playlistDao: PlaylistsDao,
    private val sharedPlaylistsRepositoryMedia: PlaylistsRepositoryMedia,
    private val context: Context
) : PlaylistRepository {

    private val fileStorageService = FileStorageService(context)

    override suspend fun getPlaylists(): List<PlaylistForPlayer> {
        return playlistDao.getAllPlaylists()
            .first() // собираем первый эмитированный элемент (актуальный список плейлистов)
            .map { playlistEntity ->
                val domainPlaylist = playlistEntity.toDomain()
                PlaylistForPlayer.fromDomain(domainPlaylist)
            }
    }

    override suspend fun addTrackToPlaylist(playlistId: String, track: Track): AddTrackStatus {
        try {
            // Проверяем, есть ли трек уже в плейлисте
            val playlistIdLong = playlistId.toLongOrNull()
                ?: return AddTrackStatus.ERROR
            val isTrackExists = playlistDao.isTrackInPlaylist(playlistId, track.trackId)
            if (isTrackExists > 0) {
                return AddTrackStatus.ALREADY_EXISTS
            }

            // Создаём сущность для таблицы playlist_tracks
            val playlistTrackEntity = PlaylistTrackEntity(
                id = System.currentTimeMillis(),
                playlistId = playlistIdLong,
                trackId = track.trackId,
                title = track.trackName,
                artist = track.artistName,
                duration = (track.trackTimeMillis?.div(1000) ?: 0).toInt(),
                addedAt = System.currentTimeMillis()
            )

            // Сохраняем трек в новую таблицу
            playlistDao.insertTrackToPlaylist(playlistTrackEntity)

            // Обновляем счётчик треков в плейлисте
            playlistDao.incrementTrackCount(playlistIdLong)

            return AddTrackStatus.SUCCESS
        } catch (e: Exception) {
            Log.e("PlaylistRepositoryImpl", "Ошибка при добавлении трека в плейлист", e)
            return AddTrackStatus.ERROR
        }
    }


    override suspend fun createPlaylist(name: String, coverPath: String?): Long {
        // Копируем файл обложки в приватное хранилище приложения
        val savedCoverPath = coverPath?.let { fileStorageService.copyToPrivateStorage(it) }

        // Создаём доменную модель плейлиста
        val newDomainPlaylist = Playlist(
            id = "",
            name = name,
            coverPath = savedCoverPath,
            trackCount = 0,
            description = null,
            createdAt = System.currentTimeMillis(),
            trackIds = "[]"
        )

        // Преобразуем в PlaylistEntity для сохранения в БД
        val newPlaylistEntity = newDomainPlaylist.toEntity()

        // Сохраняем через общий репозиторий и возвращаем ID в виде строки
        return sharedPlaylistsRepositoryMedia.addPlaylist(newDomainPlaylist)
    }
}