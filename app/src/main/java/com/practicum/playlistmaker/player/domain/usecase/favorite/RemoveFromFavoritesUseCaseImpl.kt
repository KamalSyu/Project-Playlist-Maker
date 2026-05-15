package com.practicum.playlistmaker.player.domain.usecase.favorite

import com.practicum.playlistmaker.player.domain.repository.FavoriteTracksRepository

class RemoveFromFavoritesUseCaseImpl(
    private val favoriteTracksRepository: FavoriteTracksRepository
) : RemoveFromFavoritesUseCase {

    override suspend operator fun invoke(trackId: String): Result<Unit> {
        return try {
            favoriteTracksRepository.removeTrackFromFavorites(trackId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}