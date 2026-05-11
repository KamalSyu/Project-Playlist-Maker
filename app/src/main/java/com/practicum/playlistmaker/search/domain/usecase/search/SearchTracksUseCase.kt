package com.practicum.playlistmaker.search.domain.usecase.search

import com.practicum.playlistmaker.core.models.Track
import kotlinx.coroutines.flow.Flow

interface SearchTracksUseCase {
    operator fun invoke(query: String): Flow<Result<List<Track>>>
}