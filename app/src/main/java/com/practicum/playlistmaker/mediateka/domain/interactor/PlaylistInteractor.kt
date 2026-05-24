package com.practicum.playlistmaker.mediateka.domain.interactor

import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.core.models.domain.AddTrackStatus


interface PlaylistInteractor {
    suspend fun addTrackToPlaylist(playlistId: String, track: Track): AddTrackStatus
}
