package com.practicum.playlistmaker.search.domain.usecase.history

import com.practicum.playlistmaker.core.models.Track
import kotlinx.coroutines.flow.Flow

interface GetSearchHistoryUseCase {
    operator fun invoke(): Flow<List<Track>>
}