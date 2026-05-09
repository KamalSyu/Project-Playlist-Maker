package com.practicum.playlistmaker.player.domain.usecase

import com.practicum.playlistmaker.core.contract.RemoveFromFavoritesUseCaseContract
import com.practicum.playlistmaker.player.domain.repository.FavoriteTracksRepository

class RemoveFromFavoritesUseCase(
    private val favoriteTracksRepository: FavoriteTracksRepository
) : RemoveFromFavoritesUseCaseContract {

    override suspend operator fun invoke(trackId: String): Result<Unit> {
        return try {
            favoriteTracksRepository.removeTrackFromFavorites(trackId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
