package com.practicum.playlistmaker.player.domain.usecase

import com.practicum.playlistmaker.core.contract.ToggleFavoriteUseCaseContract
import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.player.data.repository.FavoriteRepository

class ToggleFavoriteUseCase(
    private val repository: FavoriteRepository
) : ToggleFavoriteUseCaseContract {

    override suspend operator fun invoke(track: Track): Result<Boolean> {
        return try {
            if (track.isFavorite) {
                repository.removeFromFavorites(track.trackId)
                track.isFavorite = false
                Result.success(false)
            } else {
                repository.addToFavorites(track)
                track.isFavorite = true
                Result.success(true)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun isFavorite(trackId: String): Boolean {
        return repository.isTrackFavorite(trackId)
    }
}
