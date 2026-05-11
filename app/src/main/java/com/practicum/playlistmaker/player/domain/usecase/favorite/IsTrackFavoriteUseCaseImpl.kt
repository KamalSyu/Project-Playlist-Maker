package com.practicum.playlistmaker.player.domain.usecase.favorite

import com.practicum.playlistmaker.player.domain.repository.FavoriteTracksRepository

class IsTrackFavoriteUseCaseImpl(
    private val favoriteTracksRepository: FavoriteTracksRepository
) : IsTrackFavoriteUseCase {

    override suspend operator fun invoke(trackId: String): Boolean {
        return favoriteTracksRepository.isTrackFavorite(trackId)
    }
}