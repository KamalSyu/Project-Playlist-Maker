package com.practicum.playlistmaker.search.domain.usecase.history

import com.practicum.playlistmaker.core.models.Track

interface AddTrackToHistoryUseCase {
    suspend operator fun invoke(track: Track)
}