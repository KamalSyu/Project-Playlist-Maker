package com.practicum.playlistmaker.mediateka.domain.interactor

import android.util.Log
import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.core.models.domain.AddTrackStatus
import com.practicum.playlistmaker.core.models.domain.Playlist
import com.practicum.playlistmaker.mediateka.data.db.PlaylistEntity
import com.practicum.playlistmaker.mediateka.data.db.PlaylistsDao
import com.practicum.playlistmaker.mediateka.domain.repository.PlaylistsRepositoryMedia
import com.practicum.playlistmaker.mediateka.domain.usecase.AddTrackToPlaylistUseCase

class PlaylistInteractorImpl(
    private val addTrackToPlaylistUseCase: AddTrackToPlaylistUseCase,
    private val playlistDao: PlaylistsDao,
    private val playlistsRepositoryMedia: PlaylistsRepositoryMedia
) : PlaylistInteractor {

    companion object {
        private const val TAG = "PlaylistInteractorImpl"
    }

    override suspend fun addTrackToPlaylist(playlistId: String, track: Track): AddTrackStatus {
        val playlistIdLong = playlistId.toLongOrNull()
        return if (playlistIdLong != null) {
            addTrackToPlaylistUseCase(playlistIdLong, track)
        } else {
            Log.w(TAG, "Некорректный ID плейлиста: $playlistId")
            AddTrackStatus.ERROR
        }
    }

    override suspend fun renamePlaylist(playlistId: String, newName: String) {
        val idLong = playlistId.toLongOrNull() ?: run {
            Log.e(TAG, "Не удалось преобразовать ID плейлиста в Long: $playlistId")
            return
        }
        playlistDao.updatePlaylistName(idLong, newName)
    }

    override suspend fun deletePlaylist(playlistId: String) {
        val idLong = playlistId.toLongOrNull() ?: run {
            Log.e(TAG, "Не удалось преобразовать ID плейлиста в Long: $playlistId")
            return
        }
        playlistDao.deletePlaylistById(idLong)
    }

    override suspend fun createPlaylist(name: String, coverPath: String?) {
        val newPlaylist = Playlist(
            id = "0",
            name = name,
            description = null,
            coverPath = coverPath,
            trackCount = 0,
            createdAt = System.currentTimeMillis()
        )
        playlistsRepositoryMedia.addPlaylist(newPlaylist)
    }
}
