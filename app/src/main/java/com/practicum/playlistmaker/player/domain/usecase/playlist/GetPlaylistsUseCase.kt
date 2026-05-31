package com.practicum.playlistmaker.player.domain.usecase.playlist

import com.practicum.playlistmaker.player.domain.model.PlaylistForPlayer
import kotlinx.coroutines.flow.Flow

interface GetPlaylistsUseCase {
    suspend operator fun invoke(): Flow<List<PlaylistForPlayer>>
}
