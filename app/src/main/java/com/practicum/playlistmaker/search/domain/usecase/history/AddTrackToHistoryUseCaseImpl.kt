package com.practicum.playlistmaker.search.domain.usecase.history

import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.search.domain.repository.HistoryRepository

class AddTrackToHistoryUseCaseImpl (
    private val historyRepository: HistoryRepository
) : AddTrackToHistoryUseCase {

    override suspend operator fun invoke(track: Track) {
        historyRepository.addTrack(track)
    }
}