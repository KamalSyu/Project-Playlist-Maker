package com.practicum.playlistmaker.player.domain.usecase.favorite

import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.player.domain.repository.FavoriteTracksRepository

class AddToFavoritesUseCaseImpl(
    private val favoriteTracksRepository: FavoriteTracksRepository
) : AddToFavoritesUseCase {

    override suspend operator fun invoke(track: Track): Result<Unit> {
        return try {
            favoriteTracksRepository.addTrackToFavorites(track)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}