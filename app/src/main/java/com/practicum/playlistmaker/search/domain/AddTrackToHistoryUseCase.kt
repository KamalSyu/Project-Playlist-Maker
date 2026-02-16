package com.practicum.playlistmaker.search.domain

import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.search.domain.repository.HistoryRepository
import com.practicum.playlistmaker.AddTrackToHistoryUseCaseContract
import javax.inject.Inject

class AddTrackToHistoryUseCase @Inject constructor(
    private val historyRepository: HistoryRepository
) : AddTrackToHistoryUseCaseContract {

    override suspend operator fun invoke(track: Track) {
        historyRepository.addTrack(track)
    }
}