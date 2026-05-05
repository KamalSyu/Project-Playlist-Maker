package com.practicum.playlistmaker.search.domain.usecase

import com.practicum.playlistmaker.core.contract.ClearSearchHistoryUseCaseContract
import com.practicum.playlistmaker.search.domain.repository.HistoryRepository

class ClearSearchHistoryUseCase (
    private val historyRepository: HistoryRepository
) : ClearSearchHistoryUseCaseContract {

    override suspend operator fun invoke() {
        historyRepository.clearHistory()
    }
}