package com.practicum.playlistmaker.mediateka.domain.usecase

import androidx.lifecycle.LiveData
import com.practicum.playlistmaker.mediateka.domain.model.PlaylistForMediateka
import com.practicum.playlistmaker.mediateka.domain.repository.PlaylistsRepository

class LoadPlaylistsUseCase(
    private val playlistsRepository: PlaylistsRepository
) {
    operator fun invoke(): LiveData<List<PlaylistForMediateka>> {
        return playlistsRepository.getPlaylists()
    }
}
