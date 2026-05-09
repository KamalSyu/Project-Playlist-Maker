package com.practicum.playlistmaker.player.domain.usecase

import com.practicum.playlistmaker.core.contract.IsTrackFavoriteUseCaseContract
import com.practicum.playlistmaker.player.domain.repository.FavoriteTracksRepository

class IsTrackFavoriteUseCase(
    private val favoriteTracksRepository: FavoriteTracksRepository
) : IsTrackFavoriteUseCaseContract {

    override suspend operator fun invoke(trackId: String): Boolean {
        return favoriteTracksRepository.isTrackFavorite(trackId)
    }
}
