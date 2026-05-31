package com.practicum.playlistmaker.mediateka.domain.interactor

import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.core.models.domain.AddTrackStatus
import com.practicum.playlistmaker.mediateka.domain.usecase.AddTrackToPlaylistUseCase

class PlaylistInteractorImpl(
    private val addTrackToPlaylistUseCase: AddTrackToPlaylistUseCase
) : PlaylistInteractor {
    override suspend fun addTrackToPlaylist(playlistId: String, track: Track): AddTrackStatus {
        val playlistIdLong = playlistId.toLongOrNull()
        return if (playlistIdLong != null) {
            addTrackToPlaylistUseCase(playlistIdLong, track)
        } else {
            AddTrackStatus.ERROR
        }
    }
}
