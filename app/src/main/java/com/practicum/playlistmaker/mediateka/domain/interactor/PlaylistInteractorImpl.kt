package com.practicum.playlistmaker.mediateka.domain.interactor

import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.core.models.domain.AddTrackStatus
import com.practicum.playlistmaker.core.models.domain.Playlist
import com.practicum.playlistmaker.mediateka.data.mapper.toDomain
import com.practicum.playlistmaker.mediateka.domain.repository.PlaylistsRepositoryMedia
import com.practicum.playlistmaker.mediateka.domain.usecase.AddTrackToPlaylistUseCase

class PlaylistInteractorImpl(
    private val addTrackToPlaylistUseCase: AddTrackToPlaylistUseCase,
    private val playlistsRepositoryMedia: PlaylistsRepositoryMedia
) : PlaylistInteractor {

    override suspend fun addTrackToPlaylist(playlistId: String, track: Track): AddTrackStatus {
        val playlistIdLong = playlistId.toLongOrNull()
        return if (playlistIdLong != null) {
            addTrackToPlaylistUseCase(playlistIdLong, track)
        } else {
            AddTrackStatus.ERROR
        }
    }

    override suspend fun renamePlaylist(
        playlistId: String,
        newName: String
    ) {

        val idLong = playlistId.toLongOrNull() ?: return

        playlistsRepositoryMedia.updatePlaylistName(
            id = idLong,
            newName = newName
        )
    }

    override suspend fun deletePlaylist(playlistId: String) {

        val idLong = playlistId.toLongOrNull() ?: return

        playlistsRepositoryMedia.deletePlaylistAndCleanup(idLong)
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

    override suspend fun getPlaylistById(playlistId: Long): com.practicum.playlistmaker.core.models.domain.Playlist? {
        return playlistsRepositoryMedia.getPlaylistById(playlistId)?.toDomain()
    }

    override suspend fun updatePlaylistFull(
        playlistId: Long,
        name: String,
        description: String?,
        coverPath: String?
    ) {
        val coverUri = if (!coverPath.isNullOrBlank()) {
            try {
                android.net.Uri.parse(coverPath)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }

        val result = playlistsRepositoryMedia.updatePlaylist(
            id = playlistId,
            name = name,
            description = description ?: "",
            coverUri = coverUri
        )

        if (result.isFailure) {
            throw result.exceptionOrNull() ?: Exception("Не удалось обновить плейлист")
        }
    }
}
