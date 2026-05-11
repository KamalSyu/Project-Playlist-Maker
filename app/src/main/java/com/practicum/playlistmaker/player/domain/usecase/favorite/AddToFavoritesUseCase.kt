package com.practicum.playlistmaker.player.domain.usecase.favorite

import com.practicum.playlistmaker.core.models.Track

interface AddToFavoritesUseCase {
    suspend operator fun invoke(track: Track): Result<Unit>
}