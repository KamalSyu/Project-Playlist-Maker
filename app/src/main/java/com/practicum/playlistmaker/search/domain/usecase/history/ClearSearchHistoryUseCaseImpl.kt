package com.practicum.playlistmaker.search.domain.usecase.history

import com.practicum.playlistmaker.core.contract.ClearSearchHistoryUseCaseContract
import com.practicum.playlistmaker.search.domain.repository.HistoryRepository

class ClearSearchHistoryUseCaseImpl (
    private val historyRepository: HistoryRepository
) : ClearSearchHistoryUseCase {

    override suspend operator fun invoke() {
        historyRepository.clearHistory()
    }
}