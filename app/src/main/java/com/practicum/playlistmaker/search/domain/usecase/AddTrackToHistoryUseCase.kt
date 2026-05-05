package com.practicum.playlistmaker.search.domain.usecase

import com.practicum.playlistmaker.core.contract.AddTrackToHistoryUseCaseContract
import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.search.domain.repository.HistoryRepository

class AddTrackToHistoryUseCase (
    private val historyRepository: HistoryRepository
) : AddTrackToHistoryUseCaseContract {

    override suspend operator fun invoke(track: Track) {
        historyRepository.addTrack(track)
    }
}