package com.practicum.playlistmaker.player.domain.usecase.favorite

import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.player.domain.repository.FavoriteTracksRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

class GetFavoriteTracksUseCaseImpl(
    private val favoriteTracksRepository: FavoriteTracksRepository
) : GetFavoriteTracksUseCase {

    override fun invoke(): Flow<List<Track>> {
        return favoriteTracksRepository.getFavoriteTracks()
            .flowOn(Dispatchers.IO)
    }
}