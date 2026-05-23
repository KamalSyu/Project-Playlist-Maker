package com.practicum.playlistmaker.player.data.repository

import android.util.Log
import com.practicum.playlistmaker.core.models.Playlist
import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.mediateka.data.db.PlaylistTrackEntity
import com.practicum.playlistmaker.mediateka.data.db.PlaylistsDao
import com.practicum.playlistmaker.mediateka.domain.model.PlaylistForMediateka
import com.practicum.playlistmaker.player.domain.model.PlaylistForPlayer
import com.practicum.playlistmaker.player.domain.repository.PlaylistRepository
import com.practicum.playlistmaker.mediateka.domain.repository.PlaylistsRepository

enum class AddTrackStatus { SUCCESS, ALREADY_EXISTS, ERROR }

class PlaylistRepositoryImpl(
    private val sharedPlaylistsRepository: PlaylistsRepository,
    private val playlistDao: PlaylistsDao

) : PlaylistRepository {

    override suspend fun getPlaylists(): List<PlaylistForPlayer> {
        val playlists = sharedPlaylistsRepository.getPlaylists().value ?: emptyList()
        return playlists.map { mediatekaPlaylist ->
            val domainPlaylist = PlaylistForMediateka.toDomain(mediatekaPlaylist)
            PlaylistForPlayer.fromDomain(domainPlaylist)
        }
    }

    override suspend fun addTrackToPlaylist(playlistId: String, track: Track): AddTrackStatus {
        try {
            // 1. Получаем текущий плейлист
            val playlistForMediateka = sharedPlaylistsRepository.getPlaylists().value
                ?.firstOrNull { playlist: PlaylistForMediateka -> playlist.id.toString() == playlistId }
                ?: return AddTrackStatus.ERROR

            // 2. Преобразуем в доменную модель
            val domainPlaylist = PlaylistForMediateka.toDomain(playlistForMediateka)

            // 3. Создаём сущность для таблицы playlist_tracks
            val playlistTrackEntity = PlaylistTrackEntity(
                id = System.currentTimeMillis(),
                playlistId = playlistId,
                trackId = track.trackId,
                title = track.trackName,
                artist = track.artistName,  // было: track.artist
                duration = (track.trackTimeMillis?.div(1000) ?: 0).toInt(),  // конвертируем миллисекунды в секунды и в Int
                addedAt = System.currentTimeMillis()
            )

            // 4. Сохраняем трек в новую таблицу
            playlistDao.insertTrackToPlaylist(playlistTrackEntity)

            // 5. Обновляем trackIds и trackCount в плейлисте
            val updatedTrackIds = if (domainPlaylist.trackIds == null || domainPlaylist.trackIds == "[]") {
                "[${track.trackId}]"
            } else {
                domainPlaylist.trackIds.dropLast(1) + ",${track.trackId}]"
            }
            val updatedTrackCount = domainPlaylist.trackCount + 1

            // 6. Создаём обновлённую доменную модель и сохраняем
            val updatedDomainPlaylist = domainPlaylist.copy(
                trackIds = updatedTrackIds,
                trackCount = updatedTrackCount
            )
            val updatedPlaylistForMediateka = PlaylistForMediateka.fromDomain(updatedDomainPlaylist)
            sharedPlaylistsRepository.updatePlaylist(updatedPlaylistForMediateka)

            return AddTrackStatus.SUCCESS
        } catch (e: Exception) {
            Log.e("PlaylistRepositoryImpl", "Ошибка при добавлении трека в плейлист", e)
            return AddTrackStatus.ERROR
        }
    }
    override suspend fun createPlaylist(name: String, coverPath: String?): String {
        // Создаём доменную модель плейлиста
        val newDomainPlaylist = Playlist(
            id = "",
            name = name,
            coverPath = coverPath,
            trackCount = 0,
            description = null,
            createdAt = System.currentTimeMillis(),
            trackIds = "[]"
        )
        // Преобразуем в PlaylistForMediateka для сохранения
        val newPlaylistForMediateka = PlaylistForMediateka.fromDomain(newDomainPlaylist)
        // Сохраняем через общий репозиторий и возвращаем ID в виде строки
        return sharedPlaylistsRepository.addPlaylist(newPlaylistForMediateka).toString()
    }
}
