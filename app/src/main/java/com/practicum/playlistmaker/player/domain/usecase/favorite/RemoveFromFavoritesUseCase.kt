package com.practicum.playlistmaker.player.domain.usecase.favorite

interface RemoveFromFavoritesUseCase {
    suspend operator fun invoke(trackId: String): Result<Unit>
}