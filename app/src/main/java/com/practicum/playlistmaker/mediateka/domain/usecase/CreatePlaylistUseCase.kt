package com.practicum.playlistmaker.mediateka.domain.usecase

import com.practicum.playlistmaker.mediateka.domain.model.PlaylistData
import com.practicum.playlistmaker.mediateka.domain.repository.PlaylistsRepository

class CreatePlaylistUseCase(
    private val playlistsRepository: PlaylistsRepository
) {
    suspend operator fun invoke(
        playlistName: String,
        playlistDescription: String = "",
        coverPath: String? = null
    ): Long {
        val newPlaylist = PlaylistData(
            name = playlistName,
            description = playlistDescription,
            coverPath = coverPath,
            trackIds = "[]",
            trackCount = 0,
            createdAt = System.currentTimeMillis()
        )
        return playlistsRepository.addPlaylist(newPlaylist)
    }
}
