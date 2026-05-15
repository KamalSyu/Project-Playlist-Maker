package com.practicum.playlistmaker.player.domain.usecase.favorite

import com.practicum.playlistmaker.core.models.Track

interface GetFavoriteTracksUseCase {
    operator fun invoke(): kotlinx.coroutines.flow.Flow<List<Track>>
}