package com.practicum.playlistmaker.player.domain.usecase


import com.practicum.playlistmaker.core.contract.AddToFavoritesUseCaseContract
import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.player.domain.repository.FavoriteTracksRepository

class AddToFavoritesUseCase(
    private val favoriteTracksRepository: FavoriteTracksRepository
) : AddToFavoritesUseCaseContract {

    override suspend operator fun invoke(track: Track): Result<Unit> {
        return try {
            favoriteTracksRepository.addTrackToFavorites(track)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}