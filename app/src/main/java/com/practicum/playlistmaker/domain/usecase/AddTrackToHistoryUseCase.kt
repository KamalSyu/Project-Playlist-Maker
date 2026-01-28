package com.practicum.playlistmaker.domain.usecase

import com.practicum.playlistmaker.domain.model.Track
import com.practicum.playlistmaker.domain.repository.HistoryRepository
import javax.inject.Inject

class AddTrackToHistoryUseCase @Inject constructor(private val historyRepository: HistoryRepository) {
    suspend operator fun invoke(track: Track) {
        historyRepository.addTrack(track)
    }
}