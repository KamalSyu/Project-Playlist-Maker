package com.practicum.playlistmaker.player.domain.usecase

import com.practicum.playlistmaker.core.contract.ToggleFavoriteUseCaseContract
import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.player.data.repository.FavoriteRepository

class ToggleFavoriteUseCase(
    private val repository: FavoriteRepository
) : ToggleFavoriteUseCaseContract {

    override suspend operator fun invoke(track: Track): Result<Boolean> {
        return try {
            repository.addToFavorites(track)
            track.isFavorite = true
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun isFavorite(trackId: String): Boolean {
        return repository.isTrackFavorite(trackId)
    }

    override suspend fun removeFromFavorites(trackId: String): Result<Unit> {
        return try {
            repository.removeFromFavorites(trackId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

