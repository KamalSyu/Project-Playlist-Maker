package com.practicum.playlistmaker.player.domain.usecase.favorite

interface IsTrackFavoriteUseCase {
    suspend operator fun invoke(trackId: String): Boolean
}