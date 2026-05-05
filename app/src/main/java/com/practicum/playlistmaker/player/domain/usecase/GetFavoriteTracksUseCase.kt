package com.practicum.playlistmaker.player.domain.usecase

import com.practicum.playlistmaker.core.contract.GetFavoriteTracksUseCaseContract
import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.player.data.repository.FavoriteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetFavoriteTracksUseCase(
    private val repository: FavoriteRepository
) : GetFavoriteTracksUseCaseContract {

    override fun execute(): Flow<List<Track>> {
        return repository.getFavoriteTracks().map { tracks ->
            tracks.sortedByDescending { it.addedAt }
        }
    }
}

