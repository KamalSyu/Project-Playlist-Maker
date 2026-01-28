package com.practicum.playlistmaker.domain.usecase

import com.practicum.playlistmaker.domain.repository.HistoryRepository
import javax.inject.Inject

class ClearSearchHistoryUseCase @Inject constructor (private val historyRepository: HistoryRepository) {
    suspend operator fun invoke() {
        historyRepository.clearHistory()
    }
}
