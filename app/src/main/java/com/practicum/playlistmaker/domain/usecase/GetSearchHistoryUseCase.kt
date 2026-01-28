package com.practicum.playlistmaker.domain.usecase

import com.practicum.playlistmaker.domain.model.Track
import com.practicum.playlistmaker.domain.repository.HistoryRepository
import javax.inject.Inject


class GetSearchHistoryUseCase @Inject constructor (private val historyRepository: HistoryRepository) {
    suspend operator fun invoke(): List<Track> {
        return historyRepository.getHistory()
    }
}