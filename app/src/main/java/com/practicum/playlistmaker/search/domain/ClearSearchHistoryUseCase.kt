package com.practicum.playlistmaker.search.domain

import com.practicum.playlistmaker.search.domain.repository.HistoryRepository
import com.practicum.playlistmaker.core.contract.ClearSearchHistoryUseCaseContract
import javax.inject.Inject

class ClearSearchHistoryUseCase @Inject constructor (
    private val historyRepository: HistoryRepository
) : ClearSearchHistoryUseCaseContract {

    override suspend operator fun invoke() {
        historyRepository.clearHistory()
    }
}